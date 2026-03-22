#include "riposte_engine.h"
#include <random>

constexpr static int WIN = 128;
constexpr static  uint64_t sentinelMask = 0x7F83060C183060FF;

static int maxDepth = 11;
static int allowRiposte = true;
static inline int sgn(int x)
{
    return (x > 0) - (x < 0);
}

void RiposteEngine::init()
{
    srand(time(NULL));
}

constexpr uint64_t RiposteEngine::ballMask(uint64_t set, int index)
{
    uint64_t mask = 0;
    do
    {
        mask = set & -set;
        set &= ~mask;
    } while( index-- > 0);
    return mask;
}

constexpr uint64_t RiposteEngine::take(uint64_t& set, const uint ballID) noexcept
{
    const uint64_t hotSpot = ballMask(set, ballID);
    set &= ~hotSpot;
    return hotSpot;
}

constexpr bool RiposteEngine::step(uint64_t& set1, const uint64_t set2, const uint stepID) noexcept
{
    const uint bitShift = (0x1876 >> ( (stepID & 3) << 2)) & 0xF;
    uint64_t ball = ballMask( set1, stepID >> 3 );

    const uint64_t currentPos = ball;
    set1 &= ~currentPos; // pick up the ball to move
    const uint64_t obstacles = set1 | set2 | sentinelMask;

    while( 0 == (stepID & 4) && 0 == ((ball << bitShift) & obstacles) )
    {
        ball <<= bitShift;
    }
    while( (stepID & 4)  && 0 == ((ball >> bitShift) & obstacles) )
    {
        ball >>= bitShift;
    }
    set1 |= ball; // put down the ball to the selected field
    return currentPos != ball; // valid if the ball has moved
}

constexpr int RiposteEngine::captureSearch(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, int alfa, int beta, const int depth) noexcept
{
    if( __builtin_popcountll(set2) == 4)
    {
        return WIN;
    }
    int bestScore = -WIN;
    for( int ballID: {0, 1,2,3,4} )
    {

        uint64_t nextSet2 = set2;
        const uint64_t nextSpot = take(nextSet2, ballID);
        const int score = - ( allowRiposte ? search(nextSet2, set1, nextSpot, -beta, -alfa, depth - 1) : searchRestrict(nextSet2, set1, nextSpot, -beta, -alfa, depth - 1) );

        if(bestScore < score)
        {
            bestScore = score;
        }

        alfa = std::max(alfa, score);
        if( score > 0 || alfa >= beta)
        {
            break;
        }
    }
    return bestScore - sgn(bestScore);
}

constexpr int RiposteEngine::captureRoot(const uint64_t set1, const uint64_t set2, uint64_t & hotSpot, const int depth) noexcept
{
    if( __builtin_popcountll(set2) == 4)
    {
        hotSpot = set2 & -set2;
        return WIN;
    }

    int bestScore = -WIN;
    for( int ballID: {0, 1,2,3,4} )
    {

        uint64_t nextSet2 = set2;
        const uint64_t nextSpot = take(nextSet2,ballID);

        const int score = - ( allowRiposte ? search(nextSet2, set1, nextSpot, -WIN, WIN, depth - 1) : searchRestrict(nextSet2, set1, nextSpot, -WIN, WIN, depth - 1) );

        if( bestScore < score )
        {
            bestScore = score;
            hotSpot = nextSpot;
        }
    }
    return bestScore - sgn(bestScore);
}

constexpr int RiposteEngine::searchRestrict(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, int alfa, int beta, const int depth) noexcept
{
    if( 0 == depth )
    {
        return 0;
    }

    int bestScore = -WIN;
    for(uint stepID = 0; stepID < __builtin_popcountll(set1) * 8; ++stepID)
    {
        uint64_t nextSet1 = set1;

        if( ! step(nextSet1, set2, stepID) || nextSet1 & hotSpot )
        {
            continue;
        }
        const int score = -search(set2, nextSet1, hotSpot, -beta, -alfa, depth - 1);
        bestScore = std::max(bestScore, score);
        alfa = std::max(alfa, score);

        if( score > 0 || alfa >= beta)
        {
            break;
        }
    }
    return bestScore - sgn(bestScore);

}

constexpr int RiposteEngine::search(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, int alfa, int beta, const int depth) noexcept
{
    if( 0 == depth )
    {
        return 0;
    }

    int bestScore = -WIN;
    for(uint stepID = 0; stepID < __builtin_popcountll(set1) * 8; ++stepID)
    {
        uint64_t nextSet1 = set1;

        if( ! step(nextSet1, set2, stepID) )
        {
            continue;
        }
        if( nextSet1 & hotSpot ) [[unlikely]]
        {
            const int score = captureSearch(nextSet1, set2, hotSpot, alfa, beta, depth);
            bestScore = std::max(bestScore, score);
            if( score > 0)
            {
                break;
            }
        }
        else
        {
            const int score = -search(set2, nextSet1, hotSpot, -beta, -alfa, depth - 1);
            bestScore = std::max(bestScore, score);
            alfa = std::max(alfa, score);

            if( score > 0 || alfa >= beta)
            {
                break;
            }
        }
    }
    return bestScore - sgn(bestScore);
}

MoveData RiposteEngine::searchIDA(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot)
{
    uint64_t deadBranches = 0;

    uint moveID = 0;
    MoveData bestMove;
    bool finished = false;
    for( uint idaDepth = 1; idaDepth < maxDepth && !finished; ++idaDepth )
    {
        int score = -WIN, bestScore = -WIN;

        uint count = 2;
        for(moveID = 0, score = -WIN; moveID < __builtin_popcountll(set1) * 8; ++moveID)
        {
            if( (1ULL << moveID) & deadBranches )
            {
                continue;
            }

            uint64_t nextSet1 = set1;
            if( ! step(nextSet1, set2, moveID ) )
            {
                deadBranches |= (1ULL << moveID);
                continue;
            }

            if( nextSet1 & hotSpot ) [[unlikely]]
            {
                uint64_t nextSpot = hotSpot;
                score = captureRoot(nextSet1, set2, nextSpot, idaDepth);
                if( score == bestScore && 0 == rand() % count++ )
                {
                    bestMove = getCompactMoveData( set1, nextSet1, nextSpot);
                }
                if( score > bestScore )
                {
                    bestScore = score;
                    bestMove = getCompactMoveData( set1, nextSet1, nextSpot);
                    count = 2;
                }
            }
            else
            {
                score = -search(set2, nextSet1, hotSpot, -WIN, WIN, idaDepth);
                if( score == bestScore && 0 == rand() % count++ )
                {
                    bestMove = getCompactMoveData(set1, nextSet1, hotSpot);
                }
                if( score > bestScore )
                {
                    bestScore = score;
                    bestMove = getCompactMoveData(set1, nextSet1, hotSpot);
                    count = 2;
                }
            }
            if( score < 0 )
            {
                deadBranches |= (1ULL << moveID);
            }
            if( score > 0 )
            {
                finished = true;
                break;
            }
        }
    }
    return bestMove;
}

constexpr int RiposteEngine::getIndex(uint64_t mask)
{
    const int x  = __builtin_clzll(mask) % 7 - 2;
    const int y = __builtin_clzll(mask) / 7 - 1;
    return x + 5*y;
}

constexpr MoveData RiposteEngine::getCompactMoveData(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot)
{
    MoveData move;
    const uint64_t from = ballMask( (set1 ^ set2) & set1, 0 );
    const uint64_t to   = ballMask( (set1 ^ set2) & set2, 0 );
    move[0] = getIndex(from);
    move[1] = getIndex( to );
    move[2] = getIndex(hotSpot);
    return move;
}

MoveData RiposteEngine::getBestStep(const int * board, const int playerID, const uint depth, const bool riposte)
{
    maxDepth = depth;
    allowRiposte = riposte;
    uint64_t set1 = 0;
    uint64_t set2 = 0;
    uint64_t hotSpot = 0;
    for( uint64_t bitMask = (1ULL << 56), fieldID = 0; fieldID < 35; ++fieldID, bitMask >>=1)
    {
        if( fieldID % 5 == 0 )
        {
            bitMask >>= 2;
        }
        switch(board[fieldID])
        {
            case 0:
                break;
            case 1:
            case 2:
                if( playerID == board[fieldID])
                {
                    set1 |= bitMask;
                }
                else
                {
                    set2 |= bitMask;
                }
                break;
            case 4:
                hotSpot = bitMask;
                break;
            default:
                ; // error
        }
    }

    return searchIDA(set1,set2,hotSpot);
}

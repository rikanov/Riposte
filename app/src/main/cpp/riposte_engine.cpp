#include "riposte_engine.h"
#include <random>
#include <algorithm>
#include <chrono>

thread_local uint nodeCounter = 0;

static int WIN = 128;
static uint64_t sentinelMask = 0x7F83060C183060FF;

static int maxDepth = 11;
static int allowRiposte = true;
static inline int sgn(int x) {
    return (x > 0) - (x < 0);
}
std::atomic<bool> RiposteEngine::stopSearch{false};
// --- ZOBRIST STATICS ---
uint64_t RiposteEngine::zobristP1[64];
uint64_t RiposteEngine::zobristP2[64];
uint64_t RiposteEngine::zobristHotSpot[64];
uint64_t RiposteEngine::zobristTurn;
bool RiposteEngine::isZobristInitialized = false;
RiposteEngine::TTEntry RiposteEngine::transpositionTable[TT_SIZE];

void RiposteEngine::init()
{
    if (isZobristInitialized) return;

    std::mt19937_64 rng(12345ULL); // FIX SEED
    for(int i = 0; i < 64; ++i) {
        zobristP1[i] = rng();
        zobristP2[i] = rng();
        zobristHotSpot[i] = rng();
    }
    zobristTurn = rng();

    for(int i = 0; i < TT_SIZE; ++i) {
        transpositionTable[i].hash = 0;
    }
    isZobristInitialized = true;
}

uint64_t RiposteEngine::computeHash(uint64_t set1, uint64_t set2, uint64_t hotSpot, bool isP1) noexcept
{
    uint64_t hash = isP1 ? zobristTurn : 0;

    uint64_t p1_mask = isP1 ? set1 : set2;
    uint64_t p2_mask = isP1 ? set2 : set1;

    while(p1_mask) {
        int idx = __builtin_ctzll(p1_mask);
        hash ^= zobristP1[idx];
        p1_mask &= p1_mask - 1;
    }
    while(p2_mask) {
        int idx = __builtin_ctzll(p2_mask);
        hash ^= zobristP2[idx];
        p2_mask &= p2_mask - 1;
    }
    if (hotSpot) {
        hash ^= zobristHotSpot[__builtin_ctzll(hotSpot)];
    }
    return hash;
}

constexpr uint64_t RiposteEngine::ballMask(uint64_t set, int index)
{
    uint64_t mask = 0;
    do {
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

bool RiposteEngine::step(uint64_t& set1, const uint64_t set2, const uint stepID) noexcept
{
    const uint bitShift = (0x1876 >> ( (stepID & 3) << 2)) & 0xF;
    uint64_t ball = ballMask( set1, stepID >> 3 );

    const uint64_t currentPos = ball;
    set1 &= ~currentPos;
    const uint64_t obstacles = set1 | set2 | sentinelMask;

    while( 0 == (stepID & 4) && 0 == ((ball << bitShift) & obstacles) ) {
        ball <<= bitShift;
    }
    while( (stepID & 4)  && 0 == ((ball >> bitShift) & obstacles) ) {
        ball >>= bitShift;
    }
    set1 |= ball;
    return currentPos != ball;
}

int RiposteEngine::captureSearch(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, int alfa, int beta, const int depth, bool isP1, uint64_t hash) noexcept
{
    if( __builtin_popcountll(set2) == 4) {
        return WIN;
    }
    int bestScore = -WIN;
    for( int ballID: {0, 1, 2, 3, 4} )
    {
        uint64_t nextSet2 = set2;
        const uint64_t nextSpot = take(nextSet2, ballID);

        int oldHotSpotIdx = __builtin_ctzll(hotSpot);
        int newHotSpotIdx = __builtin_ctzll(nextSpot);

        uint64_t nextHash = hash
                            ^ zobristHotSpot[oldHotSpotIdx]
                            ^ zobristHotSpot[newHotSpotIdx]
                            ^ (!isP1 ? zobristP1[newHotSpotIdx] : zobristP2[newHotSpotIdx])
                            ^ zobristTurn;

        const int score = - ( allowRiposte
                              ? search(nextSet2, set1, nextSpot, -beta, -alfa, depth - 1, !isP1, nextHash)
                              : searchRestrict(nextSet2, set1, nextSpot, -beta, -alfa, depth - 1, !isP1, nextHash) );

        if(bestScore < score) { bestScore = score; }
        alfa = std::max(alfa, score);
        if( score > 0 || alfa >= beta) { break; }
    }
    return bestScore - sgn(bestScore);
}

int RiposteEngine::captureRoot(const uint64_t set1, const uint64_t set2, uint64_t & hotSpot, const int depth, bool isP1, uint64_t hash) noexcept
{
    if( __builtin_popcountll(set2) == 4) {
        hotSpot = set2 & -set2;
        return WIN;
    }

    int bestScore = -WIN;
    for( int ballID: {0, 1, 2, 3, 4} )
    {
        uint64_t nextSet2 = set2;
        const uint64_t nextSpot = take(nextSet2,ballID);

        int oldHotSpotIdx = __builtin_ctzll(hotSpot);
        int newHotSpotIdx = __builtin_ctzll(nextSpot);

        uint64_t nextHash = hash
                            ^ zobristHotSpot[oldHotSpotIdx]
                            ^ zobristHotSpot[newHotSpotIdx]
                            ^ (!isP1 ? zobristP1[newHotSpotIdx] : zobristP2[newHotSpotIdx])
                            ^ zobristTurn;

        const int score = - ( allowRiposte
                              ? search(nextSet2, set1, nextSpot, -WIN, WIN, depth - 1, !isP1, nextHash)
                              : searchRestrict(nextSet2, set1, nextSpot, -WIN, WIN, depth - 1, !isP1, nextHash) );

        if( bestScore < score ) {
            bestScore = score;
            hotSpot = nextSpot;
        }
    }
    return bestScore - sgn(bestScore);
}

int RiposteEngine::getIndex(uint64_t mask)
{
    const int x = __builtin_clzll(mask) % 7 - 2;
    const int y = __builtin_clzll(mask) / 7 - 1;
    return x + 5 * y;
}

MoveData RiposteEngine::getCompactMoveData(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot)
{
    MoveData move;
    const uint64_t from = ballMask( (set1 ^ set2) & set1, 0 );
    const uint64_t to   = ballMask( (set1 ^ set2) & set2, 0 );
    move[0] = getIndex(from);
    move[1] = getIndex( to );
    move[2] = getIndex(hotSpot);
    return move;
}

int RiposteEngine::searchRestrict(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, int alfa, int beta, const int depth, bool isP1, uint64_t hash) noexcept
{
    if( 0 == depth ) return 0;
    if ( (++nodeCounter & 2047) == 0 && (stopSearch.load(std::memory_order_relaxed)) ) return 0;
    // --- 1. TT OLVASÁS ---
    TTEntry& ttEntry = transpositionTable[hash & 0xFFFFF];
    if (ttEntry.hash == hash && ttEntry.depth >= depth) {
        if (ttEntry.flag == FLAG_EXACT) return ttEntry.score;
        if (ttEntry.flag == FLAG_ALPHA && ttEntry.score <= alfa) return alfa;
        if (ttEntry.flag == FLAG_BETA && ttEntry.score >= beta) return beta;
    }

    int original_alfa = alfa;
    int bestScore = -WIN;

    for(uint stepID = 0; stepID < __builtin_popcountll(set1) * 8; ++stepID)
    {
        uint64_t nextSet1 = set1;

        if( ! step(nextSet1, set2, stepID) || nextSet1 & hotSpot ) {
            continue;
        }

        int fromIdx = __builtin_ctzll(set1 & ~nextSet1);
        int toIdx   = __builtin_ctzll(nextSet1 & ~set1);

        uint64_t nextHash = hash
                            ^ zobristTurn
                            ^ (isP1 ? zobristP1[fromIdx] : zobristP2[fromIdx])
                            ^ (isP1 ? zobristP1[toIdx] : zobristP2[toIdx]);

        const int score = -search(set2, nextSet1, hotSpot, -beta, -alfa, depth - 1, !isP1, nextHash);

        bestScore = std::max(bestScore, score);
        alfa = std::max(alfa, score);

        if( score > 0 || alfa >= beta) { break; }
    }

    int finalScore = bestScore - sgn(bestScore);

    // --- 2. TT ÍRÁS ---
    if (ttEntry.hash != hash || depth >= ttEntry.depth) {
        ttEntry.hash = hash;
        ttEntry.depth = depth;
        ttEntry.score = finalScore;

        if (finalScore <= original_alfa) {
            ttEntry.flag = FLAG_ALPHA;
        } else if (finalScore >= beta) {
            ttEntry.flag = FLAG_BETA;
        } else {
            ttEntry.flag = FLAG_EXACT;
        }
    }

    return finalScore;
}

int RiposteEngine::search(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, int alfa, int beta, const int depth, bool isP1, uint64_t hash) noexcept
{
    if( 0 == depth ) return 0;
    if ( (++nodeCounter & 2047) == 0 && (stopSearch.load(std::memory_order_relaxed)) ) return 0;
    // --- 1. TT CACHE ---
    TTEntry& ttEntry = transpositionTable[hash & 0xFFFFF];

    if (ttEntry.hash == hash && ttEntry.depth >= depth) {
        if (ttEntry.flag == FLAG_EXACT) return ttEntry.score;
        if (ttEntry.flag == FLAG_ALPHA && ttEntry.score <= alfa) return alfa;
        if (ttEntry.flag == FLAG_BETA && ttEntry.score >= beta) return beta;
    }

    int original_alfa = alfa;
    int bestScore = -WIN;

    for(uint stepID = 0; stepID < __builtin_popcountll(set1) * 8; ++stepID)
    {
        uint64_t nextSet1 = set1;
        if( ! step(nextSet1, set2, stepID) ) { continue; }

        int fromIdx = __builtin_ctzll(set1 & ~nextSet1);
        int toIdx   = __builtin_ctzll(nextSet1 & ~set1);

        uint64_t nextHash_base = hash
                                 ^ (isP1 ? zobristP1[fromIdx] : zobristP2[fromIdx])
                                 ^ (isP1 ? zobristP1[toIdx] : zobristP2[toIdx]);

        if( nextSet1 & hotSpot ) [[unlikely]] {
            const int score = captureSearch(nextSet1, set2, hotSpot, alfa, beta, depth, isP1, nextHash_base);
            bestScore = std::max(bestScore, score);
            if( score > 0) { break; }
        } else {
            uint64_t nextHash = nextHash_base ^ zobristTurn;
            const int score = -search(set2, nextSet1, hotSpot, -beta, -alfa, depth - 1, !isP1, nextHash);
            bestScore = std::max(bestScore, score);
            alfa = std::max(alfa, score);

            if( score > 0 || alfa >= beta) { break; }
        }
    }

    int finalScore = bestScore - sgn(bestScore);

    // --- 2. TT WRITE  ---
    if (ttEntry.hash != hash || depth >= ttEntry.depth) {
        ttEntry.hash = hash;
        ttEntry.depth = depth;
        ttEntry.score = finalScore;

        if (finalScore <= original_alfa) {
            ttEntry.flag = FLAG_ALPHA;
        } else if (finalScore >= beta) {
            ttEntry.flag = FLAG_BETA;
        } else {
            ttEntry.flag = FLAG_EXACT;
        }
    }

    return finalScore;
}
MoveData RiposteEngine::searchIDA(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, bool isP1, int threadID)
{
    uint64_t deadBranches = 0;
    MoveData bestMove;
    bool finished = false;
    uint64_t rootHash = computeHash(set1, set2, hotSpot, isP1);

    // --- ÚJ: A LEHETSÉGES KEZDŐLÉPÉSEK ÖSSZEGYŰJTÉSE ---
    std::vector<uint> rootMoves;
    for(uint moveID = 0; moveID < __builtin_popcountll(set1) * 8; ++moveID) {
        uint64_t nextSet1 = set1;
        if( step(nextSet1, set2, moveID ) ) {
            rootMoves.push_back(moveID);
        }
    }

    if (threadID > 0) {
        std::mt19937 rng(12345 + threadID);
        std::shuffle(rootMoves.begin(), rootMoves.end(), rng);
    }

    for( uint idaDepth = 1; idaDepth < maxDepth && !finished; ++idaDepth )
    {
        int score = -WIN, bestScore = -WIN;
        uint count = 2;

        for(uint moveID : rootMoves)
        {
            if (stopSearch.load(std::memory_order_relaxed)) return bestMove;

            if( (1ULL << moveID) & deadBranches ) { continue; }

            uint64_t nextSet1 = set1;
            step(nextSet1, set2, moveID);

            int fromIdx = __builtin_ctzll(set1 & ~nextSet1);
            int toIdx   = __builtin_ctzll(nextSet1 & ~set1);

            uint64_t nextHash_base = rootHash
                                     ^ (isP1 ? zobristP1[fromIdx] : zobristP2[fromIdx])
                                     ^ (isP1 ? zobristP1[toIdx] : zobristP2[toIdx]);

            if( nextSet1 & hotSpot ) [[unlikely]]
            {
                uint64_t nextSpot = hotSpot;
                score = captureRoot(nextSet1, set2, nextSpot, idaDepth, isP1, nextHash_base);

                if( score == bestScore && 0 == rand() % count++ ) {
                    bestMove = getCompactMoveData( set1, nextSet1, nextSpot);
                }
                if( score > bestScore ) {
                    bestScore = score;
                    bestMove = getCompactMoveData( set1, nextSet1, nextSpot);
                    count = 2;
                }
            }
            else
            {
                uint64_t nextHash = nextHash_base ^ zobristTurn;
                score = -search(set2, nextSet1, hotSpot, -WIN, WIN, idaDepth, !isP1, nextHash);

                if( score == bestScore && 0 == rand() % count++ ) {
                    bestMove = getCompactMoveData(set1, nextSet1, hotSpot);
                }
                if( score > bestScore ) {
                    bestScore = score;
                    bestMove = getCompactMoveData(set1, nextSet1, hotSpot);
                    count = 2;
                }
            }

            if( score < 0 ) { deadBranches |= (1ULL << moveID); }
            if( score > 0 ) { finished = true; break; }
        }
    }
    return bestMove;
}
MoveData RiposteEngine::getBestStep(const int * board, const int playerID, const uint depth, const bool riposte)
{
    init();
    maxDepth = depth;
    allowRiposte = riposte;

    uint64_t set1 = 0; uint64_t set2 = 0; uint64_t hotSpot = 0;
    for( uint64_t bitMask = (1ULL << 56), fieldID = 0; fieldID < 35; ++fieldID, bitMask >>=1) {
        if( fieldID % 5 == 0 ) { bitMask >>= 2; }
        switch(board[fieldID]) {
            case 1: case 2: if( playerID == board[fieldID]) set1 |= bitMask; else set2 |= bitMask; break;
            case 4: hotSpot = bitMask; break;
        }
    }
    bool isP1 = (playerID == 1);

    stopSearch.store(false, std::memory_order_relaxed);

    // --- WATCHDOG ---
    std::thread watchdog([&]() {
        auto start = std::chrono::steady_clock::now();
        while (!stopSearch.load(std::memory_order_relaxed)) {
            auto now = std::chrono::steady_clock::now();
            if (std::chrono::duration_cast<std::chrono::seconds>(now - start).count() >= 2+depth) {
                stopSearch.store(true, std::memory_order_relaxed);
                break;
            }
            std::this_thread::sleep_for(std::chrono::milliseconds(100));
        }
    });

    int numCores = std::thread::hardware_concurrency();
    int threadsToUse = std::max(1, std::min(4, numCores));

    std::vector<std::thread> helpers;
    for (int i = 1; i < threadsToUse; ++i) {
        helpers.emplace_back([=]() {
            searchIDA(set1, set2, hotSpot, isP1, i);
        });
    }

    MoveData bestMove = searchIDA(set1, set2, hotSpot, isP1, 0);

    stopSearch.store(true, std::memory_order_relaxed);

    // Megvárjuk a többieket
    if (watchdog.joinable()) watchdog.join();
    for (auto& t : helpers) {
        if (t.joinable()) t.join();
    }

    return bestMove;
}

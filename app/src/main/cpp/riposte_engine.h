#ifndef RIPOSTE_ENGINE_H
#define RIPOSTE_ENGINE_H

#include <cstdint>
#include <array>
#include <atomic>
#include <thread>
#include <vector>

typedef unsigned int uint;
typedef std::array<int, 3> MoveData;

class RiposteEngine {
public:
    static void init();
    static MoveData getBestStep(const int * board, const int playerID, const uint depth, const bool riposte);

private:
    // --- BITBOARD  ---
    static constexpr uint64_t ballMask(uint64_t set, int index);
    static constexpr uint64_t take(uint64_t& set, const uint ballID) noexcept;
    static bool step(uint64_t& set1, const uint64_t set2, const uint stepID) noexcept;
    static int getIndex(uint64_t mask);
    static MoveData getCompactMoveData(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot);
    // --- ZOBRIST HASHING  ---
    static uint64_t zobristP1[64];
    static uint64_t zobristP2[64];
    static uint64_t zobristHotSpot[64];
    static uint64_t zobristTurn;
    static bool isZobristInitialized;

    // --- TRANSZPOZÍTION TABLE (TT) ---
    static constexpr uint8_t FLAG_EXACT = 0;
    static constexpr uint8_t FLAG_ALPHA = 1;
    static constexpr uint8_t FLAG_BETA  = 2;

    struct TTEntry {
        uint64_t hash;
        int depth;
        int score;
        uint8_t flag;
    };

    static constexpr int TT_SIZE = 1048576; // ~24 MB RAM
    static TTEntry transpositionTable[TT_SIZE];

    // --- LAZY SMP)---
    static std::atomic<bool> stopSearch;

    // --- SEARCHING FUNCTIONS ---
    static uint64_t computeHash(uint64_t set1, uint64_t set2, uint64_t hotSpot, bool isP1) noexcept;

    static int captureSearch(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, int alfa, int beta, const int depth, bool isP1, uint64_t hash) noexcept;
    static int captureRoot(const uint64_t set1, const uint64_t set2, uint64_t & hotSpot, const int depth, bool isP1, uint64_t hash) noexcept;
    static int searchRestrict(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, int alfa, int beta, const int depth, bool isP1, uint64_t hash) noexcept;
    static int search(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, int alfa, int beta, const int depth, bool isP1, uint64_t hash) noexcept;

    static MoveData searchIDA(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, bool isP1, int threadID);
};

#endif // RIPOSTE_ENGINE_H

#ifndef __MATCHFLOW_H__
#define __MATCHFLOW_H__

#include "adapter_struct.h"
#include <cstdint>

class RiposteEngine
{
private:
    constexpr static inline uint64_t take(uint64_t & set, const uint ballID) noexcept;
    constexpr static inline bool step(uint64_t & set1, const uint64_t set2, const uint stepID) noexcept;

    constexpr static int captureSearch(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, int alfa, int beta, const int depth) noexcept;
    constexpr static int captureRoot  (const uint64_t set1, const uint64_t set2, uint64_t & hotSpot, const int depth) noexcept;

    constexpr static int search(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, int alfa, int beta, const int depth) noexcept;
    constexpr static int searchRestrict(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, int alfa, int beta, const int depth) noexcept;
    constexpr static inline uint64_t ballMask(uint64_t set, int index);
    constexpr static inline Position getCoordinates(uint64_t mask);
    constexpr static inline MoveData getCompactMoveData( const uint64_t set1, const uint64_t set2, const uint64_t hotSpot);

    static MoveData searchIDA(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot);

public:
    RiposteEngine() = delete;
    static MoveData getBestStep(const int *, const int playerID, const uint depth, const bool riposte);
};

#endif

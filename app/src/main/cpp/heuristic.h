#ifndef RIPOSTE_HEURISTIC_H
#define RIPOSTE_HEURISTIC_H

#include <__algorithm/max.h>
#include <cstdint>

namespace Heuristic {
    constexpr static int WIN = 1280;
    constexpr static int heuristicLow = -1024;
    constexpr static int heuristicHigh = 1024;
    constexpr static uint64_t sentinelMask = 0x7F83060C183060FF;

    constexpr int rayCasting(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot) {
        int result = 0;

        const uint64_t obstacles = set1 | set2 | sentinelMask;
        const uint64_t obs1 = set1 | sentinelMask;
        const uint64_t obs2 = set2 | sentinelMask;
        for (int step: {8, 7, 6, 1}) {
            const uint64_t front = hotSpot >> step;
            const int multiplier = (front & obstacles) ? ((front & set1) != 0) + 2 : 1;
            for (uint64_t position = hotSpot << step; (position & obs2) == 0; position <<= step) {
                result += position & set1 ? multiplier : 0;
            }
        }

        for (int step: {8, 7, 6, 1}) {
            const uint64_t front = hotSpot << step;
            const int multiplier = (front & obstacles) ? ((front & set1) != 0) + 2 : 1;
            for (uint64_t position = hotSpot >> step; (position & obs2) == 0; position >>= step) {
                result += position & set1 ? multiplier : 0;
            }
        }
        return result;
    }

    constexpr int heuristic(const uint64_t set1, const uint64_t set2, const uint64_t hotSpot, const int offW = 10, const int defW = 10) {
        constexpr uint64_t chessMask1 = 0x5555555555555555;
        constexpr uint64_t chessMask2 = 0xAAAAAAAAAAAAAAAA;

        int hs1 = std::max(__builtin_popcountll((set1 | hotSpot) & chessMask1),
                           __builtin_popcountll((set1 | hotSpot) & chessMask2));
        int hs2 = std::max(__builtin_popcountll((set2 | hotSpot) & chessMask1),
                           __builtin_popcountll((set2 | hotSpot) & chessMask2));

        hs1 += rayCasting(set1, set2, hotSpot);
        hs2 += rayCasting(set2, set1, hotSpot);

        return (hs1 * offW) - (hs2 * defW);
    }
}
#endif //RIPOSTE_HEURISTIC_H

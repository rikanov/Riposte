#ifndef __ADAPTER_STRUCT_H__
#define __ADAPTER_STRUCT_H__

using uint = unsigned int;

struct MoveData
{
    int m_array[6] = {};

    constexpr int & operator[] (const int index)
    {
        return m_array[index];
    }
    constexpr int operator[] (const int index) const
    {
        return m_array[index];
    }
    constexpr int data (const int index) const
    {
        return m_array[index];
    }
};

struct Position
{
    uint x = 0;
    uint y = 0;
};
#endif

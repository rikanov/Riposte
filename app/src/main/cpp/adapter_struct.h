#ifndef __ADAPTER_STRUCT_H__
#define __ADAPTER_STRUCT_H__


using uint = unsigned int;


struct MoveData
{
    int indices[3] = {};

    constexpr MoveData() {};
    constexpr int & operator[] (const int index)
    {
        return indices[index];
    }
    constexpr int  operator[] (const int index) const
    {
        return indices[index];
    }
};

#endif

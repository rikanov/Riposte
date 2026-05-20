cmake_minimum_required(VERSION 3.22.1)
project("riposte")

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED True)

add_library(${CMAKE_PROJECT_NAME} SHARED
    native-lib.cpp
    riposte_engine.cpp
    riposte_tt_engine.cpp)

if(ANDROID)
    target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        log)
    else()
    # DESKTOP (LINUX/MAC/WIN)
    find_package(JNI REQUIRED)
    target_include_directories(riposte PRIVATE ${JNI_INCLUDE_DIRS})
endif()

cmake_minimum_required(VERSION 3.22.1)
project("edgeviewer")

# C++ Standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# OpenCV Path (adjust to your OpenCV SDK location)
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/../opencv-sdk/sdk/native/jni)
find_package(OpenCV REQUIRED)

# Include directories
include_directories(
        ${CMAKE_SOURCE_DIR}/../native
        ${CMAKE_SOURCE_DIR}/../native/utils
        ${OpenCV_INCLUDE_DIRS}
)

# Add native library
add_library(
        native-lib
        SHARED
        ${CMAKE_SOURCE_DIR}/../native/native-lib.cpp
        ${CMAKE_SOURCE_DIR}/../native/edge_processor.cpp
        ${CMAKE_SOURCE_DIR}/../native/utils/opencv_helpers.cpp
)

# Find required libraries
find_library(log-lib log)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)

# Link libraries
target_link_libraries(
        native-lib
        ${OpenCV_LIBS}
        ${log-lib}
        ${android-lib}
        ${jnigraphics-lib}
)
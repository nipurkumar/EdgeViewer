cmake_minimum_required(VERSION 3.22.1)
project("edgeviewer")

# Use C++17
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)



# Path to OpenCV headers
include_directories(
        ${CMAKE_SOURCE_DIR}/../native
        ${CMAKE_SOURCE_DIR}/../native/utils
        F:/OpenCV-android-sdk/sdk/native/jni/include
)

# Add native library
add_library(
        native-lib
        SHARED
        ${CMAKE_SOURCE_DIR}/../native/native-lib.cpp
        ${CMAKE_SOURCE_DIR}/../native/edge_processor.cpp
        ${CMAKE_SOURCE_DIR}/../native/utils/opencv_helpers.cpp
)

# Find standard Android libraries
find_library(log-lib log)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)

# Link libraries
target_link_libraries(
        native-lib
        ${log-lib}
        ${android-lib}
        ${jnigraphics-lib}
        # Link OpenCV shared libs (.so files in jniLibs/arm64-v8a)
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/arm64-v8a/libopencv_core.so
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/arm64-v8a/libopencv_imgproc.so
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/arm64-v8a/libopencv_imgcodecs.so
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/arm64-v8a/libopencv_video.so
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/arm64-v8a/libopencv_features2d.so
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/arm64-v8a/libopencv_calib3d.so
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/arm64-v8a/libopencv_java4.so
)

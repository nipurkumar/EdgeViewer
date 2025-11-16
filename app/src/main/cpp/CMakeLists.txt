cmake_minimum_required(VERSION 3.22.1)
project("edgeviewer")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ---- Headers (inside the APK) ----
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/include)

# ---- Sources ----
set(NATIVE_SOURCES
        native-lib.cpp
        edge_processor.cpp
        utils/opencv_helpers.cpp
)
add_library(native-lib SHARED ${NATIVE_SOURCES})

# ---- Android system libs ----
find_library(log-lib log)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)

# ---- OpenCV prebuilt libs (per ABI) ----
set(OPENCV_LIB_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}")

macro(import_opencv name)
    add_library(${name} SHARED IMPORTED)
    set_target_properties(${name} PROPERTIES
            IMPORTED_LOCATION "${OPENCV_LIB_DIR}/lib${name}.so")
endmacro()

import_opencv(opencv_core)
import_opencv(opencv_imgproc)
# import_opencv(opencv_imgcodecs)   # only if you read/write files

# ---- Link (ORDER MATTERS) ----
target_link_libraries(native-lib
        ${log-lib}
        ${android-lib}
        ${jnigraphics-lib}

        opencv_core
        opencv_imgproc
        # opencv_imgcodecs

        -latomic
        -lm
)
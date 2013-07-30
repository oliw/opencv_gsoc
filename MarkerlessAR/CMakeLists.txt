cmake_minimum_required(VERSION 2.6)
project(EXAMPLE_MARKERLESS_AR)

find_package(OpenCV REQUIRED )
find_package(OpenGL REQUIRED )
find_package(PkgConfig REQUIRED)
pkg_check_modules(TBB REQUIRED tbb)

include_directories(${OpenCV_INCLUDE_DIR})
include_directories(${OpenGL_INCLUDE_DIR})
include_directories(${tbb_INCLUDE_DIRS})

link_directories(${OpenCV_LIBRARY_DIR})
link_directories(${OpenGL_LIBRARY_DIR})
link_directories(${tbb_LIBRARY_DIRS})

include_directories(${EXAMPLE_MARKERLESS_AR_SOURCE_DIR}/src)

add_subdirectory(src)

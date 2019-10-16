
# 设置构建本机库所需的CMake最低版本。
cmake_minimum_required(VERSION 3.4.1)

# 声明一个文件夹所有的c文件
file(GLOB bzip2_resource bzip2-1.0.6/*.c)

add_library( # 设置库的名称。
        native-lib

        #将库设置为共享库。
        SHARED

        # 提供源文件的相对路径。
        native-lib.cpp
        bspatch.c
        ${bzip2_resource}
        )

# 引入一个目录
include_directories(bzip2-1.0.6)


find_library(
        log-lib
        log )

target_link_libraries(
        native-lib
        ${log-lib} )
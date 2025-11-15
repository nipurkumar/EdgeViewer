#ifndef OPENCV_HELPERS_H
#define OPENCV_HELPERS_H

#include <opencv2/core.hpp>

class OpenCVHelpers {
public:
    static void convertToRGBA(const cv::Mat& input, cv::Mat& output);
    static cv::Mat createMatFromBytes(const uint8_t* data, int width, int height, int channels);
    static void applyColorMap(const cv::Mat& input, cv::Mat& output, int colormap);
};

#endif
#include "opencv_helpers.h"
#include <opencv2/imgproc.hpp>

void OpenCVHelpers::convertToRGBA(const cv::Mat& input, cv::Mat& output) {
    if (input.channels() == 1) {
        // Grayscale to RGBA
        cv::cvtColor(input, output, cv::COLOR_GRAY2RGBA);
    } else if (input.channels() == 3) {
        // RGB to RGBA
        cv::cvtColor(input, output, cv::COLOR_RGB2RGBA);
    } else if (input.channels() == 4) {
        // Already RGBA
        output = input.clone();
    } else {
        // Unsupported format, convert to grayscale first
        cv::Mat gray;
        cv::cvtColor(input, gray, cv::COLOR_BGR2GRAY);
        cv::cvtColor(gray, output, cv::COLOR_GRAY2RGBA);
    }
}

cv::Mat OpenCVHelpers::createMatFromBytes(const uint8_t* data, int width, int height, int channels) {
    int type = CV_8UC1;
    if (channels == 3) {
        type = CV_8UC3;
    } else if (channels == 4) {
        type = CV_8UC4;
    }

    return cv::Mat(height, width, type, const_cast<uint8_t*>(data));
}

void OpenCVHelpers::applyColorMap(const cv::Mat& input, cv::Mat& output, int colormap) {
    cv::applyColorMap(input, output, colormap);
}
#include "edge_processor.h"
#include <opencv2/opencv.hpp>
#include <android/log.h>
#include "utils/opencv_helpers.h"

#define LOG_TAG "EdgeProcessor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

EdgeProcessor::EdgeProcessor() : initialized_(false) {
    initialize();
}

EdgeProcessor::~EdgeProcessor() {
    // Cleanup if needed
}

bool EdgeProcessor::initialize() {
    initialized_ = true;
    LOGD("EdgeProcessor initialized");
    return true;
}

bool EdgeProcessor::processFrame(
        const uint8_t* inputData,
        int width,
        int height,
        ProcessingMode mode,
        std::vector<uint8_t>& outputData) {

    if (!initialized_ || inputData == nullptr) {
        return false;
    }

    try {
        // Create input Mat from RGB data
        cv::Mat inputMat(height, width, CV_8UC3, const_cast<uint8_t*>(inputData));
        cv::Mat processedMat;

        // Process based on mode
        switch (mode) {
            case ProcessingMode::CANNY:
                processCanny(inputMat, processedMat);
                break;
            case ProcessingMode::SOBEL:
                processSobel(inputMat, processedMat);
                break;
            case ProcessingMode::GRAYSCALE:
                processGrayscale(inputMat, processedMat);
                break;
            default:
                processedMat = inputMat.clone();
                break;
        }

        // Convert to RGBA for OpenGL
        cv::Mat rgbaMat;
        OpenCVHelpers::convertToRGBA(processedMat, rgbaMat);

        // Copy to output vector
        outputData.resize(rgbaMat.total() * rgbaMat.channels());
        std::memcpy(outputData.data(), rgbaMat.data, outputData.size());

        return true;

    } catch (const cv::Exception& e) {
        LOGD("OpenCV exception: %s", e.what());
        return false;
    }
}

void EdgeProcessor::processCanny(const cv::Mat& input, cv::Mat& output) {
    cv::Mat gray;
    cv::cvtColor(input, gray, cv::COLOR_RGB2GRAY);

    // Apply Gaussian blur to reduce noise
    cv::GaussianBlur(gray, gray, cv::Size(5, 5), 1.4);

    // Apply Canny edge detection
    cv::Canny(gray, output, 50, 150, 3);
}

void EdgeProcessor::processSobel(const cv::Mat& input, cv::Mat& output) {
    cv::Mat gray, grad_x, grad_y, abs_grad_x, abs_grad_y;

    // Convert to grayscale
    cv::cvtColor(input, gray, cv::COLOR_RGB2GRAY);

    // Apply Gaussian blur
    cv::GaussianBlur(gray, gray, cv::Size(3, 3), 0);

    // Calculate gradients
    cv::Sobel(gray, grad_x, CV_16S, 1, 0, 3);
    cv::Sobel(gray, grad_y, CV_16S, 0, 1, 3);

    // Convert to absolute values
    cv::convertScaleAbs(grad_x, abs_grad_x);
    cv::convertScaleAbs(grad_y, abs_grad_y);

    // Combine gradients
    cv::addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, output);
}

void EdgeProcessor::processGrayscale(const cv::Mat& input, cv::Mat& output) {
    cv::cvtColor(input, output, cv::COLOR_RGB2GRAY);
}
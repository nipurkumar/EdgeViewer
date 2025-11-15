#ifndef EDGE_PROCESSOR_H
#define EDGE_PROCESSOR_H

#include <vector>
#include <opencv2/core.hpp>

enum class ProcessingMode {
    CANNY = 0,
    SOBEL = 1,
    GRAYSCALE = 2
};

class EdgeProcessor {
public:
    EdgeProcessor();
    ~EdgeProcessor();

    bool initialize();

    bool processFrame(
            const uint8_t* inputData,
            int width,
            int height,
            ProcessingMode mode,
            std::vector<uint8_t>& outputData
    );

private:
    void processCanny(const cv::Mat& input, cv::Mat& output);
    void processSobel(const cv::Mat& input, cv::Mat& output);
    void processGrayscale(const cv::Mat& input, cv::Mat& output);

    bool initialized_;
};

#endif
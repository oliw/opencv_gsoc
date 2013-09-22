/*****************************************************************************
*   Markerless AR desktop application.
******************************************************************************
*   by Khvedchenia Ievgen, 5th Dec 2012
*   http://computer-vision-talks.com
******************************************************************************
*   Ch3 of the book "Mastering OpenCV with Practical Computer Vision Projects"
*   Copyright Packt Publishing 2012.
*   http://www.packtpub.com/cool-projects-with-opencv/book
*****************************************************************************/

////////////////////////////////////////////////////////////////////
// File includes:
#include "ARPipeline.hpp"
#include <android/log.h>

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "markerless-ar-ndk", __VA_ARGS__))


ARPipeline::ARPipeline(const std::vector<cv::Mat>& patternImages, const CameraCalibration& calibration)
  : m_calibration(calibration)
{
  m_patternDetector.buildPatternsFromImages(patternImages, m_patterns);
  m_patternDetector.train(m_patterns);
}

ARPipeline::ARPipeline(const std::vector<std::string>& patternYamlPaths, const CameraCalibration& calibration)
    : m_calibration(calibration)
{
    m_patternDetector.buildPatternsFromYAML(patternYamlPaths, m_patterns);
    m_patternDetector.train(m_patterns);
}

bool ARPipeline::processFrame(const cv::Mat& inputFrame)
{
  bool patternFound = m_patternDetector.findPattern(inputFrame, m_patternInfo);
  if (patternFound)
  {
    m_patternInfo.computePose(m_patterns[m_patternInfo.patternIdx], m_calibration);
  }
  return patternFound;
}

const Transformation& ARPipeline::getPatternLocation() const
{
  return m_patternInfo.pose3d;
}

inline char separator()
{
#ifdef _WIN32
    return '\\';
#else
    return '/';
#endif
}

void ARPipeline::savePatterns(String directory) const
{
    for (int i = 0; i < m_patterns.size(); i++) {
        std::stringstream ss;
        ss << i << ".yml";
        std::string file = directory+separator()+ss.str();
        FileStorage fs(file, FileStorage::WRITE);
        Pattern pattern = m_patterns[i];
        fs << "width" << pattern.size.width;
        fs << "height" << pattern.size.height;
        fs << "keypoints" << pattern.keypoints;
        fs << "descriptors" << pattern.descriptors;
        fs.release();
    }
}


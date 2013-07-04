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

ARPipeline::ARPipeline(const std::vector<cv::Mat>& patternImages, const CameraCalibration& calibration)
  : m_calibration(calibration)
{
  m_patternDetector.buildPatternsFromImages(patternImages, m_patterns);
  m_patternDetector.train(m_patterns);
}

bool ARPipeline::processFrame(const cv::Mat& inputFrame)
{
  bool patternFound = m_patternDetector.findPattern(inputFrame, m_patternInfo);
  if (patternFound)
  {
    Pattern m_pattern = m_patternDetector.getMatchedPattern();
    m_patternInfo.computePose(m_pattern, m_calibration);
  }
  return patternFound;
}

const Transformation& ARPipeline::getPatternLocation() const
{
  return m_patternInfo.pose3d;
}

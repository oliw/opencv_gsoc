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

#ifndef ARPIPELINE_HPP
#define ARPIPELINE_HPP

////////////////////////////////////////////////////////////////////
// File includes:
#include "PatternDetector.hpp"
#include "CameraCalibration.hpp"

class ARPipeline
{
public:
  ARPipeline(const std::vector<cv::Mat>& patternImages, const CameraCalibration& calibration);

  bool processFrame(const cv::Mat& inputFrame);

  const cv::Mat_<float>& getPatternLocation() const;

  PatternDetector     m_patternDetector;

private:
  CameraCalibration   m_calibration;
  std::vector<Pattern>m_patterns;
  PatternTrackingInfo m_patternInfo;
};

#endif

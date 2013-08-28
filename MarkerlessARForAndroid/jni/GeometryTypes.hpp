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

#ifndef Example_MarkerBasedAR_GeometryTypes_hpp
#define Example_MarkerBasedAR_GeometryTypes_hpp

#include <opencv2/opencv.hpp>

using namespace cv;

struct Transformation
{
  Transformation();
  Transformation(const Matx33f& r, const Vec3f& t);
  
  Matx33f& r();
  Vec3f&  t();
  
  const Matx33f& r() const;
  const Vec3f&  t() const;
  
  Matx44f getMat44() const;
  
  Transformation getInverted() const;
private:
  Matx33f m_rotation;
  Vec3f  m_translation;
};

#endif

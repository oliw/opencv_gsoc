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
#include "GeometryTypes.hpp"

Matrix44 Matrix44::getTransposed() const
{
  Matrix44 t;
  
  for (int i=0;i<4; i++)
    for (int j=0;j<4;j++)
      t.mat[i][j] = mat[j][i];
    
  return t;
}

Matrix44 Matrix44::identity()
{
  Matrix44 eye;
  
  for (int i=0;i<4; i++)
    for (int j=0;j<4;j++)
      eye.mat[i][j] = i == j ? 1 : 0;
  
  return eye;
}

Matrix44 Matrix44::getInvertedRT() const
{
  Matrix44 t = identity();
  
  for (int col=0;col<3; col++)
  {
    for (int row=0;row<3;row++)
    { 
      // Transpose rotation component (inversion)
      t.mat[row][col] = mat[col][row];
    }
    
    // Inverse translation component
    t.mat[3][col] = - mat[3][col];
  }
  return t;
}

Transformation::Transformation()
: m_rotation(Matx33f::eye())
, m_translation(Vec3f(0,0,0))
{
  
}

Transformation::Transformation(const Matx33f& r, const Vec3f& t)
: m_rotation(r)
, m_translation(t)
{
  
}

Matx33f& Transformation::r()
{
  return m_rotation;
}

Vec3f&  Transformation::t()
{
  return  m_translation;
}

const Matx33f& Transformation::r() const
{
  return m_rotation;
}

const Vec3f&  Transformation::t() const
{
  return  m_translation;
}

Matrix44 Transformation::getMat44() const
{
  Matrix44 res = Matrix44::identity();
  
  for (int col=0;col<3;col++)
  {
    for (int row=0;row<3;row++)
    {
      // Copy rotation component
      res.mat[row][col] = m_rotation(row,col);
    }
    
    // Copy translation component
    res.mat[3][col] = m_translation[col];
  }
  
  return res;
}

Transformation Transformation::getInverted() const
{
    return Transformation(m_rotation.t(), -m_translation);
}

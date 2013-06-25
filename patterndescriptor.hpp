#ifndef PATTERNDESCRIPTOR_HPP
#define PATTERNDESCRIPTOR_HPP

#include <opencv2/core.hpp>

using namespace std;
using namespace cv;

class PatternDescriptor
{
public:
    PatternDescriptor();
    PatternDescriptor(Mat &image);

    void write(FileStorage &fs) const;
    void read(const FileNode &node);

private:
    vector<KeyPoint> keypoints;
    Mat descriptors;
};

#endif // PATTERNDESCRIPTOR_HPP

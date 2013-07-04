#include "patterndescriptor.hpp"
#include <opencv2/features2d.hpp>

static void write(FileStorage& fs, const std::string&, const PatternDescriptor& x) {
    x.write(fs);
}

static void read(const FileNode &node, PatternDescriptor& x, const PatternDescriptor& default_value = PatternDescriptor())
{
    if(node.empty()) {
        x = default_value;
    } else {
        x.read(node);
    }
}

PatternDescriptor::PatternDescriptor(Mat &image)
{
    ORB orb;
    orb(image, Mat(), keypoints, descriptors);
}

PatternDescriptor::PatternDescriptor(vector<KeyPoint> &keypoints, Mat &descriptors) : keypoints(keypoints),descriptors(descriptors){}

void PatternDescriptor::write(FileStorage &fs) const {
    fs << "{" << "keypoints" << keypoints << "descriptors" << descriptors << "}";
}

void PatternDescriptor::read(const FileNode &node) {
    node["keypoints"] >> keypoints;
    node["descriptors"] >> descriptors;
}

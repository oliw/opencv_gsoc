#include <iostream>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/features2d/features2d.hpp>

using namespace std;
using namespace cv;

int main(int argc, char** argv)
{
    ORB orb;
    string inFile = "";
    string outFile = "";
    cout << "Offline Training for Augmented Reality application" << endl;
    cout << "Compiled with OpenCV Version:"CV_VERSION << endl;

    if (argc >= 3) {
        inFile = argv[1];
        outFile = argv[argc-1];
    } else {
        cout << "Usage: ./train inFile1 inFile2 inFile3 .. outFile" << endl;
        return 1;
    }

    // Open xml file for writing
    FileStorage fs(outFile, FileStorage::WRITE);

    fs << "images" << "[";
    for (int i = 0; i < argc - 2; i++) {
        inFile = argv[i+1];

        // Load image
        Mat image;
        image = imread(inFile, IMREAD_COLOR);
        if (!image.data) {
            cout << "Could not open or find image" << endl;
            return -1;
        }
        // Find features
        // Compute descriptors
        std::vector<KeyPoint> keypoints;
        Mat descriptors;
        orb(image, Mat(), keypoints, descriptors);
        // Store in xml
        fs << "{";
        fs << "imageName" << inFile;
        fs << "descriptors" << descriptors;
        fs << "}";
    }
    fs << "]";
    fs.release();
    return 0;
}


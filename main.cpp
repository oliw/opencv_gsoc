#include <iostream>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/features2d/features2d.hpp>

using namespace std;
using namespace cv;

int train(int argc, char** argv) {
    cout << "Offline Training" << endl;
    ORB orb;
    string outFile = "";
    typedef vector<KeyPoint> KeyPoints;

    if (argc >= 4) {
        outFile = argv[argc-1];
    } else {
        cout << "Usage: ./app train inFile1 inFile2 inFile3 .. outFile" << endl;
        return 1;
    }

    int nimages = argc-3;
    Mat images[nimages];
    KeyPoints keypoints[nimages];
    Mat descriptors[nimages];

    for (int i = 0; i < nimages; i++) {
        // Load image
        string inFile = argv[2+i];
        images[i] = imread(inFile, IMREAD_COLOR);
        if (!images[i].data) {
            cout << "Could not open or find image:" << inFile << endl;
            return -1;
        }

        // Find features and compute descriptors
        orb(images[i],Mat(),keypoints[i], descriptors[i]);
    }

    // Write to file
    FileStorage fs(outFile, FileStorage::WRITE);
    fs << "images" << "[";
    for (int i = 0; i < nimages; i++) {
        fs << "{";
        fs << "descriptors" << descriptors[i];
        fs << "}";
    }
    fs << "]";
    fs.release();
    return 0;
}

int main(int argc, char** argv)
{
    cout << "Augmented Reality application" << endl;
    cout << "Compiled with OpenCV Version:"CV_VERSION << endl;

    if (argc < 2) {
        cout << "Usage: ./app command" << endl;
        return 1;
    }

    string command = argv[1];
    if (command == "train") {
        return train(argc,argv);
    } else {
        cout << "Available commands: train" << endl;
        return 1;
    }

    return 0;
}


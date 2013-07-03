#include <iostream>
#include <string>
#include "patterndescriptor.hpp"
#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

enum orb_setting {NFEATURES, SCALEFACTOR, NLEVELS, EDGETHRESHOLD, FIRSTLEVEL, WTA_K, SCORETYPE, PATCHSIZE};

orb_setting currSetting = NFEATURES;

// Orb params
int nfeatures = 500;
float scaleFactor = 1.2f;
int nlevels=8;
int edgeThreshold=31;
int firstLevel = 0;
int wtak = 2;
int scoreType = ORB::HARRIS_SCORE;
int patchSize = 31;

template <typename T>
std::string ToString(const T& value)
{
    std::ostringstream stream;
    stream << value;
    return stream.str();
}

void incrementCurrentSetting() {
    switch (currSetting) {
        case NFEATURES:
            nfeatures += 1;
            break;
        case SCALEFACTOR:
            scaleFactor += 0.05;
            break;
        case NLEVELS:
            nlevels += 1;
            break;
        case EDGETHRESHOLD:
            edgeThreshold += 1;
            break;
        case FIRSTLEVEL:
            firstLevel += 1;
            break;
        case WTA_K:
            wtak += 1;
            break;
        case SCORETYPE:
            scoreType = (scoreType == ORB::HARRIS_SCORE) ? ORB::FAST_SCORE : ORB::HARRIS_SCORE;
            break;
        case PATCHSIZE:
            patchSize += 1;
            break;
    }
}

void decrementCurrentSetting() {
    switch (currSetting) {
        case NFEATURES:
            nfeatures -= 1;
            break;
        case SCALEFACTOR:
            scaleFactor -= 0.05;
            break;
        case NLEVELS:
            nlevels -= 1;
            break;
        case EDGETHRESHOLD:
            edgeThreshold -= 1;
            break;
        case FIRSTLEVEL:
            firstLevel -= 1;
            break;
        case WTA_K:
            wtak -= 1;
            break;
        case SCORETYPE:
            scoreType = (scoreType == ORB::HARRIS_SCORE) ? ORB::FAST_SCORE : ORB::HARRIS_SCORE;
            break;
        case PATCHSIZE:
            patchSize -= 1;
            break;
    }
}

void drawText(Mat &img) {
    Scalar unselected = CV_RGB(0,200,0);
    Scalar selected = CV_RGB(200,0,0);
    Scalar current;
    current = currSetting == NFEATURES ? selected : unselected;
    putText(img, "1. Number Features: "+ ToString(nfeatures), cv::Point(10,15), CV_FONT_HERSHEY_PLAIN, 1, current);
    current = currSetting == SCALEFACTOR ? selected : unselected;
    putText(img, "2. Scale Factor: "+ ToString(scaleFactor), cv::Point(10,30), CV_FONT_HERSHEY_PLAIN, 1, current);
    current = currSetting == NLEVELS ? selected : unselected;
    putText(img, "3. Number of levels: "+ ToString(nlevels), cv::Point(10,45), CV_FONT_HERSHEY_PLAIN, 1,current);
    current = currSetting == EDGETHRESHOLD ? selected : unselected;
    putText(img, "4. Edge Theshold: "+ ToString(edgeThreshold), cv::Point(10,60), CV_FONT_HERSHEY_PLAIN, 1, current);
    current = currSetting == FIRSTLEVEL ? selected : unselected;
    putText(img, "5. First Level: "+ToString(firstLevel), cv::Point(10,75), CV_FONT_HERSHEY_PLAIN, 1, current);
    current = currSetting == WTA_K ? selected : unselected;
    putText(img, "6. WTA_K: "+ToString(wtak), cv::Point(10,90), CV_FONT_HERSHEY_PLAIN, 1, current);
    current = currSetting == SCORETYPE ? selected : unselected;
    string currentScore = (scoreType == ORB::HARRIS_SCORE)?"Harris Score":"Fast Score";
    putText(img, "7. Score Type: "+ currentScore, cv::Point(10,105), CV_FONT_HERSHEY_PLAIN, 1, current);
    current = currSetting == PATCHSIZE ? selected : unselected;
    putText(img, "8. Patch Size: "+ToString(patchSize), cv::Point(10,120), CV_FONT_HERSHEY_PLAIN, 1, current);
    putText(img, "q: Quit, r: Reset, 1-8: Select value, Arrow keys: +/-", cv::Point(10,135), CV_FONT_HERSHEY_PLAIN, 1, current);
}

int main(int argc, char** argv)
{
    cout << "Augmented Reality application" << endl;
    cout << "Compiled with OpenCV Version:"CV_VERSION << endl;

    if (argc < 2) {
        cout << "Usage: ./app imageFile" << endl;
        return 1;
    }

    // Read image
    Mat image = imread(argv[1]);

    if (image.empty()) {
        cout << "Could not read image" << endl;
        return -1;
    }

    namedWindow("Main Window");

    while (true) {
        // Detect features
        ORB orb(nfeatures,scaleFactor,nlevels,edgeThreshold,firstLevel,wtak,scoreType,patchSize);
        vector<KeyPoint> keypoints;
        Mat descriptors;
        orb(image,cv::noArray(),keypoints,descriptors);

        // Show image
        Mat dispImage = image.clone();

        // Draw Keypoints
        drawKeypoints(dispImage, keypoints, dispImage);

        drawText(dispImage);

        imshow("Main Window",dispImage);
        int keyCode = cv::waitKey(0);
        // Process keyboard event
        if (keyCode == 'q') {
            break;
        } else if (keyCode == 'r') {
            int nfeatures = 500;
            float scaleFactor = 1.2f;
            int nlevels=8;
            int edgeThreshold=31;
            int firstLevel = 0;
            int wtak = 2;
            int scoreType = ORB::HARRIS_SCORE;
            int patchSize = 31;
        } else if (keyCode == '1') {
            currSetting = NFEATURES;
        } else if (keyCode == '2') {
            currSetting = SCALEFACTOR;
        } else if (keyCode == '3') {
            currSetting = NLEVELS;
        } else if (keyCode == '4') {
            currSetting = EDGETHRESHOLD;
        } else if (keyCode == '5') {
            currSetting = FIRSTLEVEL;
        } else if (keyCode == '6') {
            currSetting = WTA_K;
        } else if (keyCode == '7') {
            currSetting = SCORETYPE;
        } else if (keyCode == '8') {
            currSetting = PATCHSIZE;
        } else if (keyCode == 65362) {
            incrementCurrentSetting();
        } else if (keyCode == 65364) {
            decrementCurrentSetting();
        }
    }
    return 0;
}


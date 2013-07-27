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
#include "PatternDetector.hpp"
#include "DebugHelpers.hpp"

////////////////////////////////////////////////////////////////////
// Standard includes:
#include <cmath>
#include <iterator>
#include <iostream>
#include <iomanip>
#include <cassert>

PatternDetector::PatternDetector(cv::Ptr<cv::FeatureDetector> detector, 
    cv::Ptr<cv::DescriptorExtractor> extractor, 
    bool ratioTest)
    : m_detector(detector)
    , m_extractor(extractor)
    , enableRatioTest(ratioTest)
    , enableHomographyRefinement(true)
    , homographyReprojectionThreshold(3)
{
}


void PatternDetector::train(const std::vector<Pattern>& patterns)
{
    // Store the pattern object
    m_patterns = patterns;

    m_matchers = std::vector<cv::Ptr<cv::DescriptorMatcher> >(patterns.size());
    for (int i = 0; i < patterns.size(); i++) {
        cv::Ptr<cv::DescriptorMatcher> matcher = new cv::BFMatcher(cv::NORM_HAMMING, false);
        matcher->clear();
        std::vector<cv::Mat> descriptors(1);
        descriptors[0] = patterns[i].descriptors.clone();
        matcher->add(descriptors);
        matcher->train();
        m_matchers[i] = matcher;
    }
}

void PatternDetector::buildPatternsFromImages(const std::vector<cv::Mat>& images, std::vector<Pattern>& patterns) const
{
    patterns.clear();
    for (int i = 0; i < images.size(); i++) {
        cv::Mat image = images[i].clone();
        Pattern pattern;
        // Store original image in pattern structure
        pattern.size = cv::Size(image.cols, image.rows);
        pattern.frame = image.clone();
        getGray(image, pattern.grayImg);

        // Build 2d and 3d contours (3d contour lie in XY plane since it's planar)
        pattern.points2d.resize(4);
        pattern.points3d.resize(4);

        // Image dimensions
        const float w = image.cols;
        const float h = image.rows;

        // Normalized dimensions:
        const float maxSize = std::max(w,h);
        const float unitW = w / maxSize;
        const float unitH = h / maxSize;

        pattern.points2d[0] = cv::Point2f(0,0);
        pattern.points2d[1] = cv::Point2f(w,0);
        pattern.points2d[2] = cv::Point2f(w,h);
        pattern.points2d[3] = cv::Point2f(0,h);

        pattern.points3d[0] = cv::Point3f(-unitW, -unitH, 0);
        pattern.points3d[1] = cv::Point3f( unitW, -unitH, 0);
        pattern.points3d[2] = cv::Point3f( unitW,  unitH, 0);
        pattern.points3d[3] = cv::Point3f(-unitW,  unitH, 0);

        extractFeatures(pattern.grayImg, pattern.keypoints, pattern.descriptors);
        patterns.push_back(pattern);
    }
}

void PatternDetector::findPatternMatch(const cv::Mat queryDescriptors, int patternIdx) {
    std::vector<cv::DMatch> matches;
    matches.clear();
    if (enableRatioTest)
    {
        std::vector< std::vector<cv::DMatch> > knnMatches;

        // To avoid NaN's when best match has zero distance we will use inversed ratio.
        const float minRatio = 1.f / 1.5f;

        // KNN match will return 2 nearest matches for each query descriptor
        m_matchers[patternIdx]->knnMatch(queryDescriptors, knnMatches, 2);

        for (size_t i=0; i<knnMatches.size(); i++)
        {
            const cv::DMatch& bestMatch   = knnMatches[i][0];
            const cv::DMatch& betterMatch = knnMatches[i][1];

            float distanceRatio = bestMatch.distance / betterMatch.distance;

            // Pass only matches where distance ratio between
            // nearest matches is greater than 1.5 (distinct criteria)
            if (distanceRatio < minRatio)
            {
                matches.push_back(bestMatch);
            }
        }
    }
    else
    {
        // Perform regular match
        m_matchers[patternIdx]->match(queryDescriptors, matches);
    }
    cv::Mat roughHomography;
    // Estimate Homography for pattern and discard outlier matches
    bool homographyFoundinPattern = refineMatchesWithHomography(
        m_queryKeypoints,
        m_patterns[patternIdx].keypoints,
        homographyReprojectionThreshold,
        matches,
        roughHomography);

    // Save matches and homography found
    m_matches_mutex.lock();
    m_matches[patternIdx] = matches;
    m_matches_homography[patternIdx] = roughHomography;
    m_matches_homographyFound[patternIdx] = homographyFoundinPattern;
    m_matches_mutex.unlock();
}

bool PatternDetector::findPattern(const cv::Mat& image, PatternTrackingInfo& info)
{
    // Convert input image to gray
    getGray(image, m_grayImg);
    
    // Extract feature points from input gray image
    extractFeatures(m_grayImg, m_queryKeypoints, m_queryDescriptors);

    // Match query against each pattern in parallel
    m_matches = std::vector<std::vector<cv::DMatch> >(m_patterns.size());
    m_matches_homographyFound = std::vector<bool>(m_patterns.size());
    m_matches_homography = std::vector<cv::Mat>(m_patterns.size());
    boost::thread_group threads;
    for (int i = 0; i < m_patterns.size(); i++) {
        threads.add_thread(new boost::thread(&PatternDetector::findPatternMatch,this, m_queryDescriptors, i));
    }
    threads.join_all();
    // Process results
    bool homographyFound = false;
    int maxFound = 0;
    int maxFoundIdx = -1;
    for (int i = 0; i < m_patterns.size(); i++) {
        if (m_matches_homographyFound[i]) {
            if (m_matches[i].size() > maxFound) {
                maxFound = m_matches[i].size();
                maxFoundIdx = i;
                homographyFound = true;
            }
        }
    }

    if (!homographyFound) {
        return false;
    }

    m_roughHomography = m_matches_homography[maxFoundIdx];
    m_pattern = m_patterns[maxFoundIdx];

    // The best fitting pattern has been detected matchedPatternIdx, m_roughHomography, homographyFound, m_pattern has been set

#if _DEBUG
    cv::showAndSave("Raw matches", getMatchesImage(image, m_pattern.frame, m_queryKeypoints, m_pattern.keypoints, m_matches_i, 100));
#endif

#if _DEBUG
    cv::Mat tmp = image.clone();
#endif

    if (homographyFound)
    {
        info.patternIdx = maxFoundIdx;

#if _DEBUG
        cv::showAndSave("Refined matches using RANSAC", getMatchesImage(image, m_pattern.frame, m_queryKeypoints, m_pattern.keypoints, m_matches_i, 100));
#endif
        // If homography refinement enabled improve found transformation
        if (enableHomographyRefinement)
        {
            // Warp image using found homography
            cv::warpPerspective(m_grayImg, m_warpedImg, m_roughHomography, m_pattern.size, cv::WARP_INVERSE_MAP | cv::INTER_CUBIC);
#if _DEBUG
            cv::showAndSave("Warped image",m_warpedImg);
#endif
            // Get refined matches:
            cv::Mat m_newQueryDescriptors;
            std::vector<cv::KeyPoint> warpedKeypoints;
            std::vector<cv::DMatch> refinedMatches;

            // Detect features on warped image
            extractFeatures(m_warpedImg, warpedKeypoints, m_newQueryDescriptors);

            // Match with pattern
            getMatches(m_newQueryDescriptors, refinedMatches, maxFoundIdx);

            // Estimate new refinement homography
            homographyFound = refineMatchesWithHomography(
                warpedKeypoints,
                m_pattern.keypoints,
                homographyReprojectionThreshold,
                refinedMatches,
                m_refinedHomography);

#if _DEBUG
            cv::showAndSave("MatchesWithRefinedPose", getMatchesImage(m_warpedImg, m_pattern.grayImg, warpedKeypoints, m_pattern.keypoints, refinedMatches, 100));
#endif
            // Get a result homography as result of matrix product of refined and rough homographies:
            info.homography = m_roughHomography * m_refinedHomography;

            // Transform contour with rough homography
#if _DEBUG
            cv::perspectiveTransform(m_pattern.points2d, info.points2d, m_roughHomography);
            info.draw2dContour(tmp, CV_RGB(0,200,0));
#endif

            // Transform contour with precise homography
            cv::perspectiveTransform(m_pattern.points2d, info.points2d, info.homography);
#if _DEBUG
            info.draw2dContour(tmp, CV_RGB(200,0,0));
#endif
        }
        else
        {
            info.homography = m_roughHomography;

            // Transform contour with rough homography
            cv::perspectiveTransform(m_pattern.points2d, info.points2d, m_roughHomography);
#if _DEBUG
            info.draw2dContour(tmp, CV_RGB(0,200,0));
#endif
        }
    }

#if _DEBUG
    if (1)
    {
        cv::showAndSave("Final matches", getMatchesImage(tmp, m_pattern.frame, m_queryKeypoints, m_pattern.keypoints, m_matches_i, 100));
    }
    std::cout << "Features:" << std::setw(4) << m_queryKeypoints.size() << " Matches: " << std::setw(4) << m_matches_i.size() << std::endl;
#endif
    return homographyFound;
}

void PatternDetector::getGray(const cv::Mat& image, cv::Mat& gray)
{
    if (image.channels()  == 3)
        cv::cvtColor(image, gray, cv::COLOR_BGR2GRAY);
    else if (image.channels() == 4)
        cv::cvtColor(image, gray, cv::COLOR_BGRA2GRAY);
    else if (image.channels() == 1)
        gray = image;
}

bool PatternDetector::extractFeatures(const cv::Mat& image, std::vector<cv::KeyPoint>& keypoints, cv::Mat& descriptors) const
{
    assert(!image.empty());
    assert(image.channels() == 1);

    m_detector->detect(image, keypoints);
    if (keypoints.empty())
        return false;

    m_extractor->compute(image, keypoints, descriptors);
    if (keypoints.empty())
        return false;

    return true;
}

void PatternDetector::getMatches(const cv::Mat& queryDescriptors, std::vector<cv::DMatch>& matches, int patternIdx)
{
    matches.clear();

    if (enableRatioTest)
    {
        std::vector< std::vector<cv::DMatch> > knnMatches;

        // To avoid NaN's when best match has zero distance we will use inversed ratio.
        const float minRatio = 1.f / 1.5f;

        // KNN match will return 2 nearest matches for each query descriptor
        m_matchers[patternIdx]->knnMatch(queryDescriptors, knnMatches, 2);

        for (size_t i=0; i<knnMatches.size(); i++)
        {
            const cv::DMatch& bestMatch   = knnMatches[i][0];
            const cv::DMatch& betterMatch = knnMatches[i][1];

            float distanceRatio = bestMatch.distance / betterMatch.distance;

            // Pass only matches where distance ratio between
            // nearest matches is greater than 1.5 (distinct criteria)
            if (distanceRatio < minRatio)
            {
                matches.push_back(bestMatch);
            }
        }
    }
    else
    {
        // Perform regular match
        m_matchers[patternIdx]->match(queryDescriptors, matches);
    }
}

bool PatternDetector::refineMatchesWithHomography
    (
    const std::vector<cv::KeyPoint>& queryKeypoints,
    const std::vector<cv::KeyPoint>& trainKeypoints, 
    float reprojectionThreshold,
    std::vector<cv::DMatch>& matches,
    cv::Mat& homography
    )
{
    const int minNumberMatchesAllowed = 25;

    if (matches.size() < minNumberMatchesAllowed)
        return false;

    // Prepare data for cv::findHomography
    std::vector<cv::Point2f> srcPoints(matches.size());
    std::vector<cv::Point2f> dstPoints(matches.size());

    for (size_t i = 0; i < matches.size(); i++)
    {
        srcPoints[i] = trainKeypoints[matches[i].trainIdx].pt;
        dstPoints[i] = queryKeypoints[matches[i].queryIdx].pt;
    }

    // Find homography matrix and get inliers mask
    std::vector<unsigned char> inliersMask(srcPoints.size());
    homography = cv::findHomography(srcPoints, 
                                    dstPoints, 
                                    cv::FM_RANSAC,
                                    reprojectionThreshold, 
                                    inliersMask);

    // findHomography has a bug and sometimes returns an empty matrix
    if (homography.empty()) {
        homography = cv::Mat::eye(3,3, CV_64FC1);
    }

    std::vector<cv::DMatch> inliers;
    for (size_t i=0; i<inliersMask.size(); i++)
    {
        if (inliersMask[i])
            inliers.push_back(matches[i]);
    }

    matches.swap(inliers);
    return matches.size() > minNumberMatchesAllowed;
}

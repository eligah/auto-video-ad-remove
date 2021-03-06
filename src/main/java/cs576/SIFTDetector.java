package cs576;

import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

public class SIFTDetector {
    private MatOfKeyPoint Icon1KeyPoints;
    private MatOfKeyPoint Icon1Descriptors;
    private MatOfKeyPoint Icon2KeyPoints;
    private MatOfKeyPoint Icon2Descriptors;

    private FeatureDetector featureDetector;
    private DescriptorExtractor descriptorExtractor;
    private DescriptorMatcher descriptorMatcher;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public SIFTDetector(String icon1, String icon2) {
        featureDetector = FeatureDetector.create(FeatureDetector.SIFT);
        descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_L1);

        Mat iconImage1 = Highgui.imread(icon1, Highgui.CV_LOAD_IMAGE_COLOR);
        Mat iconImage2 = Highgui.imread(icon2, Highgui.CV_LOAD_IMAGE_COLOR);

        Icon1KeyPoints = new MatOfKeyPoint();
        Icon1Descriptors = new MatOfKeyPoint();
        Icon2KeyPoints = new MatOfKeyPoint();
        Icon2Descriptors = new MatOfKeyPoint();
        getDescripter(iconImage1, Icon1KeyPoints, Icon1Descriptors);
        getDescripter(iconImage2, Icon2KeyPoints, Icon2Descriptors);
    }

    private void getDescripter(Mat m, MatOfKeyPoint objectKeyPoints, MatOfKeyPoint objectDescriptors) {
        featureDetector.detect(m, objectKeyPoints);
        descriptorExtractor.compute(m, objectKeyPoints, objectDescriptors);
    }

    public boolean detectIcon(BufferedImage img, int choice) {
        Mat m = Utils.img2Mat(img);
        if (choice == 1) return detectIconImplementation(m, 1);
        if (choice == 2) return detectIconImplementation(m, 2);
        return false;
    }

    private boolean detectIconImplementation(Mat m, int choices) {
        MatOfKeyPoint senceKeyPoints = new MatOfKeyPoint();
        MatOfKeyPoint senceDescriptors = new MatOfKeyPoint();

        getDescripter(m, senceKeyPoints, senceDescriptors);
        if (senceDescriptors.empty()) {
            return false;
        }

        List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
        LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();
        if (choices == 1) {
            descriptorMatcher.knnMatch(Icon1Descriptors, senceDescriptors, matches, 2);
            findGoodMatches(matches, goodMatchesList, 0.75f);
            return goodMatchesList.size() > 10;
        } else if (choices == 2) {
            descriptorMatcher.knnMatch(Icon2Descriptors, senceDescriptors, matches, 2);
            findGoodMatches(matches, goodMatchesList, 0.65f);
            return goodMatchesList.size() > 10;
        } else {
            return false;
        }
    }

    private void findGoodMatches(List<MatOfDMatch> matches, LinkedList<DMatch> goodMatchesList, float nndrRatio) {
        //find the good match
        for (MatOfDMatch matofDMatch : matches) {
            DMatch[] dmatcharray = matofDMatch.toArray();
            DMatch m1 = dmatcharray[0];
            DMatch m2 = dmatcharray[1];

            if (m1.distance <= m2.distance * nndrRatio) {
                goodMatchesList.addLast(m1);
            }
        }
    }
}
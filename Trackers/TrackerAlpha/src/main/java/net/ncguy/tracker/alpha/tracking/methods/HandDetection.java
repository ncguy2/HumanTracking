package net.ncguy.tracker.alpha.tracking.methods;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.opencv.core.Core.inRange;

public class HandDetection extends DetectionMethod {

    @Override
    public Mat Process(Mat input) {
        matsToRelease.clear();


        Function<Boolean, Mat> newMat = NewFrameFunc(input);
        Mat frame = newMat.apply(false);

        input.copyTo(frame);

        Mat blur = newMat.apply(true);
        Imgproc.blur(input, blur, new Size(3, 3));

        Mat hsv = newMat.apply(true);
        Imgproc.cvtColor(blur, hsv, Imgproc.COLOR_BGR2HSV);

        Mat mask2 = newMat.apply(true);
        inRange(hsv, new Scalar(2, 50, 50), new Scalar(15, 255, 255), mask2);

        Mat kernelSquare = Mat.ones(11, 11, CvType.CV_8U);
        Mat kernelEllipse = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));


        Mat dilation = newMat.apply(true);
        Imgproc.dilate(mask2, dilation, kernelEllipse);
        Mat erosion = newMat.apply(true);
        Imgproc.erode(mask2, erosion, kernelSquare);
        Imgproc.dilate(erosion, dilation, kernelEllipse);
        Imgproc.medianBlur(dilation, blur, 5);

        kernelEllipse.release();
        kernelEllipse = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(8, 8));
        Mat dilation2 = newMat.apply(true);
        Imgproc.dilate(blur, dilation2, kernelEllipse);
        kernelEllipse.release();
        kernelEllipse = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Mat dilation3 = newMat.apply(true);
        Imgproc.dilate(blur, dilation3, kernelEllipse);

        Mat median = newMat.apply(true);
        Imgproc.medianBlur(dilation2, median, 5);

        Mat thresh = newMat.apply(true);
        double ret = Imgproc.threshold(median, thresh, 127, 255, 0);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = newMat.apply(true);
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = 100.0;
        int ci = 0;
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint cnt = contours.get(i);
            double area = Imgproc.contourArea(cnt);
            if(area > maxArea) {
                maxArea = area;
                ci = i;
            }
        }

        MatOfPoint largestContour = contours.get(ci);

        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(largestContour, hull);

        MatOfInt4 defects = new MatOfInt4();
        Imgproc.convexityDefects(largestContour, hull, defects);

        List<double[]> farDefects = new ArrayList<>();
        for (int i = 0; i < defects.rows(); i++) {
            double[] doubles = defects.get(i, 0);
            double s = doubles[0];
            double e = doubles[1];
            double f = doubles[2];
            double d = doubles[3];

            double[] start = largestContour.get((int) s, 0);
            double[] end = largestContour.get((int) e, 0);
            double[] far = largestContour.get((int) f, 0);
            farDefects.add(far);

            Imgproc.line(frame, new Point(start[0], start[1]), new Point(end[0], end[1]), new Scalar(0, 255, 0), 1);
            Imgproc.circle(frame, new Point(far[0], far[1]), 10, new Scalar(0, 255, 0), 1);
        }

        Moments moments = Imgproc.moments(largestContour);
        int cx = 0;
        int cy = 0;
        if(moments.m00 != 0.0) {
            cx = (int) (moments.m10 / moments.m00);
            cy = (int) (moments.m01 / moments.m00);
        }
        Point centerMass = new Point(cx, cy);

        Imgproc.circle(frame, centerMass, 7, new Scalar(100, 0, 255), 2);
        int font = Highgui.CV_FONT_BLACK;
        Imgproc.putText(frame, "Center", centerMass, font, 2, new Scalar(255, 255, 255), 2);

        List<Double> distanceBetweenDefectsToCenter = new ArrayList<>();
        for (int i = 0; i < farDefects.size(); i++) {
            double[] x = farDefects.get(i);
            double distance = Math.sqrt(Math.pow(x[0] - centerMass.x, 2) + Math.pow(x[1] - centerMass.y, 2));
            distanceBetweenDefectsToCenter.add(distance);
        }

        distanceBetweenDefectsToCenter.sort(Double::compareTo);
        int count = distanceBetweenDefectsToCenter.size();
        Double averageDefectDistance = distanceBetweenDefectsToCenter.stream()
                .limit(3)
                .reduce(Double::sum)
                .map(d -> d / count)
                .orElse(distanceBetweenDefectsToCenter.get(0));

        List<Integer> hullIntegers = hull.toList();
        List<double[]> finger = new ArrayList<>();
        for (int i = 0; i < hullIntegers.size() - 1; i++) {
            double[] here = hull.get(i, 0);
            double[] next = hull.get(i + 1, 0);
            if(Math.abs(here[0] - next[0]) > 80) {
                if(here[0] < 500)
                    finger.add(here);
            }
        }

        finger.sort(Comparator.comparingDouble(d -> d[0]));
        List<double[]> fingers = finger.stream().limit(5).collect(Collectors.toList());

        List<Double> fingerDistance = new ArrayList<>();
        for (double[] fing : fingers) {
            double distance = Math.sqrt(Math.pow(fing[0] - centerMass.x, 2) + Math.pow(fing[0] - centerMass.y, 2));
            fingerDistance.add(distance);
        }

        int result = 0;
        for (int i = 0; i < fingers.size(); i++) {
            if(fingerDistance.get(i) > averageDefectDistance + 130)
                result++;
        }


        Imgproc.putText(frame, String.valueOf(result), new Point(100,100), font, 2, new Scalar(255,255,255),2);

        Rect rect = Imgproc.boundingRect(largestContour);
        Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 2);

        Clear();

        defects.release();
        hull.release();
        contours.forEach(Mat::release);

        return frame;
    }


}

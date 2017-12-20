package net.ncguy.tracker.alpha.tracking.methods;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FaceDetection extends DetectionMethod {

    @Override
    public Mat Process(Mat input) {

        Function<Boolean, Mat> newMat = NewFrameFunc(input);

        List<Point3> modelPoints = new ArrayList<>();

        modelPoints.add(new Point3(-36.9522f, 39.3518f, 47.1217f));
        modelPoints.add(new Point3(35.446f, 38.4345f, 47.6468f));
        modelPoints.add(new Point3(-0.0697709f, 18.6015f, 87.9695f));
        modelPoints.add(new Point3(-27.6439f, -29.6388f, 73.8551f));
        modelPoints.add(new Point3(-87.2155f, 15.5829f, -45.1352f));
        modelPoints.add(new Point3(85.8383f, 14.9023f, -46.3169f));

        Point3[] points = new Point3[modelPoints.size()];
        modelPoints.toArray(points);
        MatOfPoint3 model = new MatOfPoint3(points);

        Scalar m = Core.mean(model);

//        Core.divide(model, new Scalar(35), model);

        Point3 rv = new Point3();
        MatOfPoint3 rvec = new MatOfPoint3(rv);
        double[] d = new double[] {
                1,  0,  0,
                0, -1,  0,
                0,  0, -1
        }; // Rotation, looking at -Z
        Mat mat = Mat.zeros(3, 3, CvType.CV_64FC1);

        mat.put(0, 0, 1);
        mat.put(1, 1, -1);
        mat.put(2, 2, -1);

        Calib3d.Rodrigues(mat, rvec);
        Point3 tv = new Point3();
        MatOfPoint3 tvec = new MatOfPoint3(tv);

        Mat camMatrix = new Mat(3, 3, CvType.CV_64FC1);



        Clear();

        return null;
    }
}

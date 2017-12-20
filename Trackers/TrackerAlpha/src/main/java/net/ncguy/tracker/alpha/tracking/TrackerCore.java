package net.ncguy.tracker.alpha.tracking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import net.ncguy.api.data.TrackedBuffer;
import net.ncguy.api.data.TrackedPoint;
import net.ncguy.tracker.alpha.cv.ImShow;
import net.ncguy.tracker.alpha.tracking.methods.ObjectDistance;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

//import edu.mit.ll.mitie.*;

public class TrackerCore {

    ImShow window;

    CascadeClassifier haarPalm;
    CascadeClassifier haarFist;
    CascadeClassifier haarGest;
    CascadeClassifier haarFace;
    CascadeClassifier haarPalm2;
    CascadeClassifier haarEye;

    CascadeClassifier haarLeftEye;
    CascadeClassifier haarRightEye;

    CascadeClassifier lbpFace;

    public TrackerCore() {
        System.out.println("OpenCV " + Core.VERSION + " loaded");
        haarPalm = new CascadeClassifier("haar/palm.xml");
        haarFist = new CascadeClassifier("haar/fist.xml");
        haarGest = new CascadeClassifier("haar/aGest.xml");
        haarFace = new CascadeClassifier("haar/face.xml");
        haarPalm2 = new CascadeClassifier("haar/palm2.xml");
        haarEye = new CascadeClassifier("haar/eye.xml");

        haarLeftEye = new CascadeClassifier("haar/haarcascade_lefteye_2splits.xml");
        haarRightEye = new CascadeClassifier("haar/haarcascade_righteye_2splits.xml");

        lbpFace = new CascadeClassifier("haar/lbp/lbpcascade_frontalface.xml");

//        System.out.println("loading NER model...");
//        NamedEntityExtractor ner = new NamedEntityExtractor("../Trackers/TrackerAlpha/libs/MITIE-Java/MITIE-models/english/ner_model.dat");
//
//        System.out.println("Tags output by this NER model are: ");
//        StringVector possibleTags = ner.getPossibleNerTags();
//        for (int i = 0; i < possibleTags.size(); ++i)
//            System.out.println(possibleTags.get(i));
//
//
//        // Load a text file and convert it into a list of words.
//        StringVector words = mitie.tokenize(mitie.loadEntireFile("../Trackers/TrackerAlpha/libs/MITIE-Java/sample_text.txt"));
//
//
//        // Now ask MITIE to find all the named entities in the file we just loaded.
//        EntityMentionVector entities = ner.extractEntities(words);
//        System.out.println("Number of entities found: " + entities.size());
//
//        // Now print out all the named entities and their tags
//        for (int i = 0; i < entities.size(); ++i)
//        {
//            // Each EntityMention contains three integers.  The start and end define the
//            // range of tokens in the words vector that are part of the entity.  There is
//            // also a tag which indicates which element of possibleTags is associated with
//            // the entity.  So we can print out all the tagged entities as follows:
//            EntityMention entity = entities.get(i);
//            String tag = possibleTags.get(entity.getTag());
//            System.out.print("Entity tag: " + tag + "\t Entity text: ");
//            printEntity(words, entity);
//        }

    }

    protected Function<Mat, Texture> matToTexFunc;
    protected Consumer<Texture> previewFunc;
    protected FrameSource frameSource;

    public void FrameSource(FrameSource source) {
        this.frameSource = source;
    }

    public void MatToTexFunc(Function<Mat, Texture> matToTexFunc) {
        this.matToTexFunc = matToTexFunc;
    }

    public void PreviewFunc(Consumer<Texture> previewFunc) {
        this.previewFunc = previewFunc;
    }

    public void SetPreview(Mat mat) {
        SetPreview(mat, true);
    }

    public void SetPreview(Mat mat, boolean release) {
//        ImShow.ImShow("Preview", mat);
        Gdx.app.postRunnable(() -> {
            this.previewFunc.accept(this.matToTexFunc.apply(mat));
            if(release)
                mat.release();
        });
    }

    public void Track(TrackedBuffer buffer) {

        frameSource.Invalidate();

        Mat frame = frameSource.GetFrame();

        Mat process = new ObjectDistance().Process(frame);



//        Detect(process, process, haarPalm, new Scalar(255, 0, 0));
//        Detect(frame, process, haarPalm2, new Scalar(0, 255, 255));
//        Detect(frame, process, haarFist, new Scalar(0, 255, 0));
//        Detect(frame, process, haarGest, new Scalar(0, 0, 255));
        Detect(frame, process, lbpFace, new Scalar(255, 0, 255), buffer);

//        for (Rect face : faces) {
//
//            Vector2 origin = new Vector2(face.x, face.y);
//            origin.add(face.width * .5f, face.height * .5f);
//
//            CameraFrameSource src = (CameraFrameSource) this.frameSource;
//
//            origin.y = src.frameHeight - origin.y;
//
//            origin.x /= src.frameWidth;
//            origin.y /= src.frameHeight;
//
//            origin.scl(2).sub(1, 1);
//
//            buffer.add(origin, TrackedPoint.Hint.HEAD);
//        }

        SetPreview(process, true);

//        SetPreview(frame, false);
//        process.release();

//        Mat target = new Mat(frame.rows(), frame.cols(), frame.type());
//        Mat grey = new Mat(frame.rows(), frame.cols(), frame.type());
//        Imgproc.cvtColor(frame, grey, Imgproc.COLOR_BGR2GRAY);
//
//        frame.copyTo(target);
//
//        Imgproc.threshold(grey, grey, 127, 255, Imgproc.THRESH_BINARY);
//
//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//
//        Imgproc.findContours(grey, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        Imgproc.drawContours(target, contours, -1, new Scalar(255, 0, 0));
//
////        Imgproc.cvtColor(target, frame, Imgproc.COLOR_GRAY2RGB);
//
//        SetPreview(target, true);
//
//        for (MatOfPoint contour : contours)
//            contour.release();
//        contours.clear();
//        grey.release();
//        hierarchy.release();
    }

    public Rect[] Detect(Mat src, Mat dst, CascadeClassifier haar, Scalar colour) {
        return Detect(src, dst, haar, colour, null);
    }

    public Rect[] Detect(Mat src, Mat dst, CascadeClassifier haar, Scalar colour, TrackedBuffer buffer) {
        MatOfRect items = new MatOfRect();
        Mat greyFrame = new Mat();

        Imgproc.cvtColor(src, greyFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(greyFrame, greyFrame);

        Rect detected = null;

        Imgproc.threshold(greyFrame, greyFrame, 96, 255, Imgproc.THRESH_BINARY_INV);
        Imgproc.blur(greyFrame, greyFrame, new Size(5, 5));

        Mat strel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(greyFrame, greyFrame, Imgproc.MORPH_OPEN, strel);
        strel.release();

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        double angle = 0;
        Imgproc.findContours(greyFrame, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        double largestArea = 0;

        int maxWidth = src.width();
        int maxHeight = src.height();

        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            double area = Imgproc.contourArea(contour);
            if(area < 50 || area > 1e5) continue;

            if(area > largestArea) {
                largestArea = area;
                Imgproc.drawContours(dst, contours, i, new Scalar(0, 0, 255), 2, 8, hierarchy, 0, new Point());
                List<Point> points = contour.toList();
                double a = GetOrientation(points, dst);
                Rect r = Imgproc.boundingRect(contour);

                Mat roi = GetRoi(src, r);

                float absoluteItemSize = 5.f;

                Imgproc.rectangle(dst, r.tl(), r.br(), new Scalar(255, 0, 0), 2);

                haar.detectMultiScale(roi, items, 1.1, 0, Objdetect.CASCADE_DO_ROUGH_SEARCH | Objdetect.CASCADE_FIND_BIGGEST_OBJECT, new Size(), new Size());
                List<Rect> rects = items.toList();
                if(!rects.isEmpty()) {
                    detected = rects.get(0);

                    detected.x += r.x;
                    detected.y += r.y;

                    angle = a;
                }
            }
        }

//        haar.detectMultiScale(src, items, 1.1, 0, Objdetect.CASCADE_FIND_BIGGEST_OBJECT, new Size(), new Size());
        haar.detectMultiScale(src, items);
        List<Rect> rects = items.toList();
        Rect largest = null;
        for (Rect rect : rects) {
            if(largest == null) {
                largest = rect;
            }else if(largest.area() < rect.area()) {
                largest = rect;
            }
        }

        if(largest != null) {
            Imgproc.rectangle(dst, largest.tl(), largest.br(), new Scalar(0, 255, 0), 2);
            detected = largest;
        }

        if(detected != null) {
//            Imgproc.rectangle(dst, detected.tl(), detected.br(), colour, 2);

            if (buffer != null)
                buffer.add(ToNDC(detected), TrackedPoint.Hint.HEAD).angle = (float) Math.toDegrees(angle) - 90;
        }

        greyFrame.release();
        items.release();
        contours.forEach(Mat::release);
        hierarchy.release();

        return null;
    }

    double GetOrientation(List<Point> points, Mat img) {
        Mat dataPts = new Mat(points.size(), 2, CvType.CV_64FC1);
        for (int i = 0; i < dataPts.rows(); i++) {
            dataPts.put(i, 0, points.get(i).x);
            dataPts.put(i, 1, points.get(i).y);
        }

        Mat mean = new Mat();
        Mat eigenVectors = new Mat();

        Core.PCACompute(dataPts, mean, eigenVectors);

        Point pos = new Point(mean.get(0, 0)[0], mean.get(0, 1)[0]);

        List<Point> eigenVecs = new ArrayList<>();
        List<Double> eigenVal = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            eigenVecs.add(new Point(eigenVectors.get(i, 0)[0], eigenVectors.get(i, 1)[0]));
            eigenVal.add(eigenVectors.get(0, i)[0]);
        }

        return Math.atan2(eigenVecs.get(0).y, eigenVecs.get(0).x);

    }

    Rect[] DetectFace(CascadeClassifier haar, Mat greyFrame, Mat dst, Scalar colour, TrackedBuffer buffer) {
        MatOfRect items = new MatOfRect();

        float absoluteItemSize = 50.f;

        haar.detectMultiScale(greyFrame, items, 1.3, 3, Objdetect.CASCADE_DO_ROUGH_SEARCH | Objdetect.CASCADE_FIND_BIGGEST_OBJECT, new Size(absoluteItemSize, absoluteItemSize), new Size());

        Rect[] rects = items.toArray();

        for (Rect rect : rects) {

            Rect r = new Rect(rect.x - 48, rect.y - 48, rect.width + 96, rect.height + 140);

            Imgproc.rectangle(dst, r.tl(), r.br(), colour, 3);

            Mat roiGrey = GetRoi(greyFrame, r);
            Mat roiCol = GetRoi(dst, r);
            MatOfRect lEyes = new MatOfRect();
            MatOfRect rEyes = new MatOfRect();

            haarLeftEye.detectMultiScale(roiGrey, lEyes);
            haarRightEye.detectMultiScale(roiGrey, rEyes);

            for (Rect eyeRect : lEyes.toList())
                DrawEye(eyeRect, roiCol, colour, buffer, TrackedPoint.Hint.LEFT_EYE);

            for (Rect eyeRect : rEyes.toList())
                DrawEye(eyeRect, roiCol, colour, buffer, TrackedPoint.Hint.RIGHT_EYE);

//            haarEye.detectMultiScale(roiGrey, eyes);



//            Imgproc.drawContours(dst, contours, -1, colour);

//            double maxVal = 0;
//            int maxValIdx = 0;
//            for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++)
//            {
//                double contourArea = Imgproc.contourArea(contours.get(contourIdx));
//                if (maxVal < contourArea) {
//                    maxVal = contourArea;
//                    maxValIdx = contourIdx;
//                }
//            }
//
//            MatOfPoint max = contours.get(maxValIdx);
//            List<Point> points = max.toList();
//
//            points.sort(Comparator.comparingDouble(p -> p.x));
//            points.sort(Comparator.comparingDouble(p -> p.y));
//
////            for (Point point : points) {
//                Imgproc.circle(dst, points.get(0), 4, colour);
//                Imgproc.circle(dst, points.get(points.size() - 1), 4, colour);
////            }
//
//            hierarchy.release();
//            contours.forEach(Mat::release);
//            threshold.release();

//            int idx = 0;
//            for (Rect eye : eyes.toArray()) {
//                Imgproc.rectangle(roiCol, eye.tl(), eye.br(), colour, 1);
//
//                if(buffer != null)
//                    buffer.add(ToNDC(eye), TrackedPoint.Hint.EYE, (idx == 0 ? TrackedPoint.Hint.LEFT_EYE : (idx == 1 ? TrackedPoint.Hint.RIGHT_EYE : null)));
//
//                idx++;
////                cv.rectangle(roi_color,(ex,ey),(ex+ew,ey+eh),(0,255,0),2)
//            }

            if(buffer != null)
                buffer.add(ToNDC(rect), TrackedPoint.Hint.HEAD);
        }

        return rects;
    }

    void DrawEye(Rect rect, Mat dst, Scalar colour, TrackedBuffer buffer, TrackedPoint.Hint... hints) {
        Imgproc.rectangle(dst, rect.tl(), rect.br(), colour, 1);

        if(buffer != null)
            buffer.add(ToNDC(rect), hints);
    }

    void RotateImage(Mat src, Mat dst, double angle) {
        if(angle == 0) return;
        Size size = src.size();
        Mat rotMat = Imgproc.getRotationMatrix2D(new Point(size.width * .5f, size.height * .5f), angle, .9);
        Imgproc.warpAffine(src, dst, rotMat, size, Imgproc.INTER_LINEAR);
    }

    Rect RotatePoint(Rect pos, Mat src, double angle) {
        if(angle == 0) return pos;
        double x = pos.x - src.size().height * .4;
        double y = pos.y - src.size().width  * .4;

        double rad = Math.toRadians(angle);

        double newX = x * Math.cos(rad) + y * Math.sin(rad) + src.size().height * .4f;
        double newY = -x * Math.sin(rad) + y * Math.cos(rad) + src.size().width * .4f;
        return new Rect((int) newX, (int) newY, pos.width, pos.height);
    }

    public void Shutdown() {

    }

    public Vector2 ToNDC(Rect rect) {
        Vector2 ndc = new Vector2(rect.x, rect.y);
        ndc.add(rect.width * .5f, rect.height * .5f);

        CameraFrameSource src = (CameraFrameSource) this.frameSource;

        ndc.y = src.frameHeight - ndc.y;

        ndc.x /= src.frameWidth;
        ndc.y /= src.frameHeight;

        return ndc.scl(2).sub(1, 1);
    }

    public Mat GetRoi(Mat src, Rect rect) {
        return new Mat(src, rect);
    }
//
//    public static void printEntity (StringVector words, EntityMention ent) {
//        // Print all the words in the range indicated by the entity ent.
//        for (int i = ent.getStart(); i < ent.getEnd(); ++i)
//            System.out.print(words.get(i) + " ");
//        System.out.println("");
//    }

}

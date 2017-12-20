package net.ncguy.tracker.alpha.cv;

/*
 * Author: ATUL
 * Thanks to Daniel Baggio , Jan Monterrubio and sutr90 for improvements
 * This code can be used as an alternative to imshow of OpenCV for JAVA-OpenCv
 * Make sure OpenCV Java is in your Build Path
 * Usage :
 * -------
 * Imshow ims = new Imshow("Title");
 * ims.showImage(Mat image);
 * Check Example for usage with Webcam Live Video Feed
 */

//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import javax.imageio.ImageIO;
//import javax.swing.plaf.ButtonUI;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class ImShow {

    static Map<String, Map.Entry<ImageIcon, JLabel>> map = new HashMap<>();

    public static void ImShow(String title, Mat mat) {
        BufferedImage img = Convert(mat);
        if(map.containsKey(title)) {
            Map.Entry<ImageIcon, JLabel> entry = map.get(title);
            ImageIcon icon = entry.getKey();
            icon.setImage(img);
            entry.getValue().updateUI();
            return;
        }

        JFrame frame = new JFrame();
        ImageIcon imageIcon = new ImageIcon(img);
        JLabel label = new JLabel(imageIcon);
        frame.getContentPane().add(label);
        frame.pack();
        frame.setTitle(title);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                map.remove(frame.getTitle());
            }
        });

        map.put(title, new AbstractMap.SimpleEntry<>(imageIcon, label));
    }

    static BufferedImage Convert(Mat mat) {
        BufferedImage img = null;
        try{
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", mat, matOfByte);
            byte[] bytes = matOfByte.toArray();
            InputStream in = new ByteArrayInputStream(bytes);
            img = ImageIO.read(in);
        }catch (Exception e) {
            e.printStackTrace();
        }

        return img;
    }

}

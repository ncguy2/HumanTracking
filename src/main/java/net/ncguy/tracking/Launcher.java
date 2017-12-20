package net.ncguy.tracking;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import net.ncguy.api.data.TrackedBuffer;
import net.ncguy.api.data.TrackedPoint;
import net.ncguy.utils.FileUtils;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.*;

public class Launcher {

    static Random rand = new Random();

    public static void main(String[] args) {

        int samplePoints = 7;
        int maxHints = 5;

        TrackedBuffer buffer = new TrackedBuffer();


        for (int i = 0; i < samplePoints; i++) {
            int hintCount = rand.nextInt(maxHints);
            List<TrackedPoint.Hint> vals = new ArrayList<>();
            Collections.addAll(vals, TrackedPoint.Hint.values());
            TrackedPoint.Hint[] sample = Sample(hintCount, TrackedPoint.Hint.class, vals);
            buffer.add(new Vector2(), sample);
        }

        ByteBuffer byteBuffer = buffer.ToByteBuffer();
        try {
            FileOutputStream s = new FileOutputStream(new File("buffer.bin"));
            FileUtils.WriteByteBuffer(byteBuffer, s);
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(name);

        new Scanner(System.in).next();

        TrackedBuffer inputBuffer = new TrackedBuffer();

        try {
            FileInputStream s = new FileInputStream(new File("buffer.bin"));
            Optional<ByteBuffer> buf = FileUtils.ReadByteBuffer(s);
            s.close();
            buf.ifPresent(inputBuffer::FromByteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Buffer 1");
        System.out.println(buffer.ToString());
        System.out.println();
        System.out.println("Buffer 2");
        System.out.println(inputBuffer.ToString());

        new Scanner(System.in).next();

        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.useOpenGL3(true, 4, 5);
        cfg.enableGLDebugOutput(true, System.out);
        cfg.setTitle("Human Tracking");
        cfg.setWindowedMode(1600, 900);
        cfg.setBackBufferConfig(16, 16, 16, 16, 16, 0, 0);
        new Lwjgl3Application(new AppListener(), cfg);
    }

    static <T> T[] Sample(int amt, Class<T> cls, T... options) {
        return Sample(amt, cls, Arrays.asList(options));
    }

    static <T> T[] Sample(int amt, Class<T> cls, List<T> options) {
        T[] arr = (T[]) Array.newInstance(cls, amt);

        for (int i = 0; i < amt; i++) {
            int idx = rand.nextInt(options.size());
            T selected = options.get(idx);
            arr[i] = selected;
            options.remove(selected);
        }

        return arr;
    }

}

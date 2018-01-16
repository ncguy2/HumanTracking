package net.ncguy.tracker.python.net;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.ncguy.api.data.TrackedBuffer;
import net.ncguy.api.data.TrackedPoint;
import net.ncguy.utils.MathsUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static net.ncguy.api.data.TrackedPoint.Hint.*;

public class NetHandler implements Runnable {

    public static final int DEFAULT_PORT = 12000;
    protected static Random random = new Random();
    private final int port;

    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;

    boolean isAlive = true;
    boolean receivingAlive = true;
    boolean transmissionAlive = true;
    boolean shouldTransmitFrame = true;

    public Supplier<FrameWrapper> bufferSupplier;
    public TrackedBuffer buffer;

    public NetHandler(TrackedBuffer buffer) throws IOException {
        this(buffer, DEFAULT_PORT);
    }

    public NetHandler(TrackedBuffer buffer, int port) throws IOException {
        this.buffer = buffer;
        this.port = port;

        socket = new Socket("127.0.0.1", port);
        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());

        Init();
    }

    public void Init() {}

    public void Accept() throws Exception {

        TransmissionOfFrames_Impl();


//        if(bufferSupplier != null)
//            StartTransmissionOfFrames();
//
//        StartReceivingOfData();
    }

    protected void StartReceivingOfData() {
        Thread t = new Thread(() -> {
            while(receivingAlive) {
                try {
                    ReceivingOfData_Impl();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    protected void ReceivingOfData_Impl() throws IOException {
        byte[] bytes = new byte[4096];
        dis.read(bytes);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        byte b = buffer.get();
        if(b == (byte) '1') {
            float focalLength = buffer.getFloat();
            float centerX = buffer.getFloat();
            float centerY = buffer.getFloat();

            float transX = buffer.getFloat();
            float transY = buffer.getFloat();
            float transZ = buffer.getFloat();

            float rotX = buffer.getFloat();
            float rotY = buffer.getFloat();
            float rotZ = buffer.getFloat();

            float angle = buffer.getFloat();

            float jawX = buffer.getFloat();
            float jawY = buffer.getFloat();
            float jawDist = buffer.getFloat();

            int payloadSize = buffer.getInt();
            int payloadStride = buffer.getInt();

            List<Vector2> screenPoints = new ArrayList<>();
            for (int i = 0; i < payloadSize * payloadStride; i += payloadStride)
                screenPoints.add(new Vector2(buffer.getFloat(), buffer.getFloat()));

            TrackedPoint p = new TrackedPoint(HEAD);
            p.useWorldCoords = true;
            p.angle = angle;
            p.worldPos.set(transX, transY, transZ);
            p.worldDir.set(rotX, rotY, rotZ);
            this.buffer.add(p);
//
//            TrackedPoint j = new TrackedPoint(TrackedPoint.Hint.JAW);
//            j.useWorldCoords = true;
//            j.GetScreenPos().set(screenPoints.get(65));
//            j.angle = jawDist;
//            this.buffer.add(j);
//
//            TrackedPoint m = new TrackedPoint(TrackedPoint.Hint.MOUTH);
//            m.useWorldCoords = true;
//            m.GetScreenPos().set(screenPoints.get(61));
//            this.buffer.add(m);
//
//            TrackedPoint ml = new TrackedPoint(TrackedPoint.Hint.MOUTH_LEFT);
//            ml.useWorldCoords = true;
//            ml.GetScreenPos().set(screenPoints.get(53));
//            this.buffer.add(ml);
//
//            TrackedPoint mr = new TrackedPoint(TrackedPoint.Hint.MOUTH_RIGHT);
//            mr.useWorldCoords = true;
//            mr.GetScreenPos().set(screenPoints.get(47));
//            this.buffer.add(mr);

            TrackedPoint globalPoint = AddPoint(GLOBAL, new Vector3(transX, transY, transZ), new Vector3(rotX, rotY, rotZ));
            globalPoint.GetScreenPos().set(centerX, centerY);
            globalPoint.angle = focalLength;

            AddPoint(JAW, screenPoints.get(56)).angle = jawDist;
            AddPoint(MOUTH, screenPoints.get(61));
            AddPoint(MOUTH_LEFT, screenPoints.get(53));
            AddPoint(MOUTH_RIGHT, screenPoints.get(47));

            AddPoint(LEFT_EYE, screenPoints.get(41));
            AddPoint(TOP_LEFT_EYE, screenPoints.get(43));
            AddPoint(LEFT_EYE_CORNER, screenPoints.get(44));
            AddPoint(BOT_LEFT_EYE, screenPoints.get(45));

            AddPoint(RIGHT_EYE_CORNER, screenPoints.get(35));
            AddPoint(TOP_RIGHT_EYE, screenPoints.get(36));
            AddPoint(RIGHT_EYE, screenPoints.get(38));
            AddPoint(BOT_RIGHT_EYE, screenPoints.get(40));
        }
    }

    protected TrackedPoint AddPoint(TrackedPoint.Hint hint, Vector2 screenPos) {
        TrackedPoint p = new TrackedPoint(hint);
        p.GetScreenPos().set(screenPos);
        this.buffer.add(p);
        return p;
    }

    protected TrackedPoint AddPoint(TrackedPoint.Hint hint, Vector3 worldPos) {
        TrackedPoint p = new TrackedPoint(hint);
        p.worldPos.set(worldPos);
        this.buffer.add(p);
        return p;
    }

    protected TrackedPoint AddPoint(TrackedPoint.Hint hint, Vector3 worldPos, Vector3 worldDir) {
        TrackedPoint p = new TrackedPoint(hint);
        p.worldPos.set(worldPos);
        p.worldDir.set(worldDir);
        this.buffer.add(p);
        return p;
    }

    protected void StartTransmissionOfFrames() {

        Thread t = new Thread(() -> {
            while(transmissionAlive) {
                if (shouldTransmitFrame) {
                    try {
                        TransmissionOfFrames_Impl();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    shouldTransmitFrame = false;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    protected void TransmissionOfFrames_Impl() throws IOException {
        byte[] propBytes;
        if(bufferSupplier != null) {
            FrameWrapper data = bufferSupplier.get();

            byte[] bytes = data.data;

            int length = bytes.length;

            byte[] width = MathsUtils.IntToByteArray(data.width);
            byte[] height = MathsUtils.IntToByteArray(data.height);
            byte[] len = MathsUtils.IntToByteArray(length);

            List<Byte> propList = new ArrayList<>();
            for (byte b : width) propList.add(b);
            for (byte b : height) propList.add(b);
            for (byte b : len) propList.add(b);

//            ArrayUtils.Concatenate(Byte.class, width, height, len);

            propBytes = new byte[propList.size()];
            for (int i = 0; i < propList.size(); i++)
                propBytes[i] = propList.get(i);
        }else {
            propBytes = new byte[] {
              0, 0, 0, 0,
              0, 0, 0, 0,
              0, 0, 0, 0
            };
        }



        dos.write(propBytes);
//        dos.write(bytes);

        ReceivingOfData_Impl();
    }

    @Override
    public void run() {
        while(isAlive) {
            try {
                Accept();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static class FrameWrapper {

        public int width;
        public int height;
        public byte[] data;

        public FrameWrapper(int width, int height, byte[] data) {
            this.width = width;
            this.height = height;
            this.data = data;
        }
    }

    public static class NetTags {
        public static final int REQUEST_FRAME = 0xFFC0FFEE;
    }

}

package net.ncguy.api.data;

import com.badlogic.gdx.math.Vector2;
import com.sun.xml.internal.ws.encoding.soap.DeserializationException;
import net.ncguy.utils.MathsUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TrackedBuffer extends ArrayList<TrackedPoint> {

    public TrackedBuffer() {
    }

    public TrackedBuffer(int initialCapacity) {
        super(initialCapacity);
    }

    public TrackedBuffer(Collection<? extends TrackedPoint> c) {
        super(c);
    }

    public String ToString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Best guesses of buffer contents\n");
        forEach(p -> sb.append("\t").append(p.GetCurrentGuess()).append("\n"));

        return sb.toString();
    }

    public TrackedPoint Get(TrackedPoint.Hint... hints) {

        if(isEmpty()) return null;

        TrackedPoint best = null;
        int bestScore = 0;

//        List<TrackedPoint> points = Collections.unmodifiableList(this);

        List<TrackedPoint> points;

        synchronized (this) {
            points = new ArrayList<>(this);
        }


        for (TrackedPoint p : points) {
            int score = 0;

            for (TrackedPoint.Hint hint : hints) {
                if(hint == null) continue;
                if(p.hints != null && p.hints.contains(hint))
                    score += hint.Score();
            }

            if(score > bestScore) {
                bestScore = score;
                best = p;
            }
        }

        return best;
    }

    public ByteBuffer ToByteBuffer() {
        return ByteBuffer.wrap(ToByteArray());
    }

    public void FromByteBuffer(ByteBuffer buffer) {
        this.clear();

        buffer.position(0);

        int bufferStart = buffer.getInt();

        if(bufferStart != BUFFER_START)
            throw new DeserializationException("Invalid start tag found");

        int pointCount = buffer.getInt();

        this.ensureCapacity(pointCount);

        TrackedPoint.Hint[] values = TrackedPoint.Hint.values();

        for (int i = 0; i < pointCount; i++) {
            int start = buffer.getInt();
            if(start != POINT_START)
                throw new DeserializationException("Invalid start tag found");

            TrackedPoint point = new TrackedPoint();
            int size = buffer.getInt();

            for (int j = 0; j < size; j++) {
                byte ordinal = buffer.get();
                TrackedPoint.Hint hint = values[ordinal];
                point.AddHint(hint);
            }

            int end = buffer.getInt();
            if(end != POINT_END)
                throw new DeserializationException("Invalid end tag found");

            add(point);
        }

        int bufferEnd = buffer.getInt();
        if(bufferEnd != BUFFER_END)
            throw new DeserializationException("Invalid end tag found");

    }

    public TrackedPoint add(Vector2 vec) {
        TrackedPoint p = new TrackedPoint();
        p.screenPos.set(vec);
        add(p);
        return p;
    }

    public TrackedPoint add(Vector2 p, TrackedPoint.Hint... hints) {
        TrackedPoint point = new TrackedPoint(hints);
        point.screenPos.set(p);
        add(point);
        return point;
    }

    protected byte[] ToByteArray() {
        List<Byte> bytes = new ArrayList<>();

        AddBytes(bytes, BUFFER_START);

        AddBytes(bytes, size());

        forEach(point -> {
            AddBytes(bytes, POINT_START);
            AddBytes(bytes, point.hints.size());

            point.hints
                    .stream()
                    .map(TrackedPoint.Hint::ToByte)
                    .forEach(bytes::add);

            AddBytes(bytes, POINT_END);
        });

        AddBytes(bytes, BUFFER_END);

        byte[] b = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++)
            b[i] = bytes.get(i);
        return b;
    }

    protected void AddBytes(List<Byte> list, int val) {
        AddBytes(list, MathsUtils.IntToByteArray(val));
    }

    protected void AddBytes(List<Byte> list, byte... bytes) {
        for (byte b : bytes)
            list.add(b);
    }

    public static final int BUFFER_START = 0x00000000;
    public static final int BUFFER_END = 0xFFFFFFFF;
    public static final int POINT_START = 0xFF00FF;
    public static final int POINT_END = 0x00FF00;

}

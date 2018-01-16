package net.ncguy.utils.collection;

import com.badlogic.gdx.math.Quaternion;

public class QuaternionQueue extends HistoryQueue<Quaternion> {

    public QuaternionQueue(int capacity) {
        super(capacity);
        SetAverager(new QuaternionAverager());
    }

    public static class QuaternionAverager implements IAverager<Quaternion> {

        @Override
        public Quaternion Average(HistoryQueue<Quaternion> queue) {
            Quaternion q = new Quaternion(queue.Peek());
            for (int i = 1; i < queue.Size(); i++)
                q = q.slerp(queue.Get(i), .5f);
            return q;
        }
    }

}

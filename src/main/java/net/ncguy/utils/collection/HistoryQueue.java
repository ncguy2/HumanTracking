package net.ncguy.utils.collection;

import java.util.LinkedList;

public class HistoryQueue<T>  {

    public int capacity;
    protected final LinkedList<T> queue;

    protected IAverager<T> averager;

    public HistoryQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new LinkedList<>();
    }

    public void SetAverager(IAverager<T> averager) {
        this.averager = averager;
    }

    public void Push(T t) {
        queue.add(t);
        while(queue.size() > capacity)
            queue.pop();
    }

    public T GetAverage() {
        if(averager != null)
            return averager.Average(this);
        return queue.get((int) (queue.size() * .5f));
    }

    public T Get(int index) {
        return queue.get(index);
    }

    public T Peek() {
        return queue.peek();
    }

    public int Size() {
        return queue.size();
    }

    public static interface IAverager<T> {

        T Average(HistoryQueue<T> queue);

    }

}

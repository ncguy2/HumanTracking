package net.ncguy.tracking.geometry;

import java.util.Objects;

public class GeometryItem {

    public transient GeometryItem parent;

    public String path;
    public GeometryItem left;
    public GeometryItem right;
    public Model data;
    public Model.Operation operation;

    public void SetNext(GeometryItem item) {

        String lastChar = item.path.substring(item.path.length() - 1);

        if(Objects.equals(lastChar, "l") || Objects.equals(lastChar, "r")) {

            if(lastChar.equals("l")) {
                left = item;
                return;
            }
            if(lastChar.equals("r")) {
                right = item;
                return;
            }

        }

        if(left == null) {
            left = item;
            return;
        }

        if(right == null) {
            right = item;
            return;
        }



        System.err.println("Item discarded");
    }

    public static class GLItem {
        public transient int parent;
        public transient String path;

        public int leftItem;
        public int rightItem;
        public int data;
        public int operation;
    }

}

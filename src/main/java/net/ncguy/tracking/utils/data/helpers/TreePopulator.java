package net.ncguy.tracking.utils.data.helpers;

import net.ncguy.tracking.utils.data.VisitableTree;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TreePopulator {

    public static <T> void Populate(VisitableTree<TreeObjectWrapper<T>> tree, List<T> data, char delim, Function<T, String> stringLoc) {
        BiFunction<T, String, TreeObjectWrapper<T>> nodeLoc = (t, s) -> {
            if(s.equals(stringLoc.apply(t))) return new TreeObjectWrapper<>(t, s);
            return new TreeObjectWrapper<>(s);
        };
        Populate(tree, data, delim, stringLoc, nodeLoc);
    }

    public static <T> void Populate(VisitableTree<TreeObjectWrapper<T>> tree, List<T> data, char delim, Function<T, String> stringLoc, BiFunction<T, String, TreeObjectWrapper<T>> nodeLoc) {
        Populate(tree, data, delim, stringLoc, nodeLoc, true);
    }

    public static <T> void Populate(VisitableTree<TreeObjectWrapper<T>> tree, List<T> data, char delim, Function<T, String> stringLoc, BiFunction<T, String, TreeObjectWrapper<T>> nodeLoc, boolean fullPath) {
        VisitableTree<TreeObjectWrapper<T>> current = tree;
        for (T datum : data) {
            VisitableTree<TreeObjectWrapper<T>> root = current;
            String pathStr = stringLoc.apply(datum);
            String[] path = pathStr.split(delim + "");
            String p = "";
            for (String cat : path) {
                if(fullPath) {
                    if (p.isEmpty()) p = cat;
                    else p += delim + cat;
                }else p = cat;
                current = current.Child(nodeLoc.apply(datum, p));
            }
            current = root;
        }
    }

}

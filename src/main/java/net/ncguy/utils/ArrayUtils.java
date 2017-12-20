package net.ncguy.utils;

import java.lang.reflect.Array;

public class ArrayUtils {

    @SuppressWarnings("unchecked")
    public static <T> T[] Prepend(T[] array, T value, Class<T> type) {
        T[] newArr = (T[]) Array.newInstance(type, array.length + 1);
        newArr[0] = value;
        System.arraycopy(array, 0, newArr, 1, array.length);
        return newArr;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] Prepend(T[] array, T value) {
        Object[] newArr = new Object[array.length + 1];
        newArr[0] = value;
        System.arraycopy(array, 0, newArr, 1, array.length);
        return (T[]) newArr;
    }

}

package net.ncguy.tracking.utils;

import java.lang.reflect.Field;
import java.util.Optional;

public class ReflectionUtils {

    public static <T> Optional<T> Get(Object object, Class<?> cls, String field, Class<T> type) {
        try {
            return Optional.ofNullable(Get_Impl(object, cls, field, type));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static <T> T Get_Impl(Object object, Class<?> cls, String field, Class<T> type) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(field);
        declaredField.setAccessible(true);
        Object o = declaredField.get(object);
        return type.cast(o);
    }

    public static void Set(Object obj, String field, Object value) {
        try {
            Set_Impl(obj, field, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void Set_Impl(Object obj, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Class<?> cls = obj.getClass();
        Field declaredField = cls.getDeclaredField(field);
        declaredField.setAccessible(true);
        declaredField.set(obj, value);
    }

}

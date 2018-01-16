package net.ncguy.api;

import javafx.beans.property.SimpleObjectProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PropertyHelper {

    public static List<PropertyWrapper> GetProperties(Class<?> cls) {
        List<PropertyWrapper> props = new ArrayList<>();

        for (Field field : cls.getDeclaredFields())
            GetFromField(field).ifPresent(props::add);

        return props;
    }

    public static Optional<PropertyWrapper> GetFromField(Field field) {
        try {
            return Optional.ofNullable(GetFromField_Impl(field));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static PropertyWrapper GetFromField_Impl(Field field) throws IllegalAccessException {
        if(!field.isAnnotationPresent(Property.class)) return null;
        int modifiers = field.getModifiers();
        if(Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
            Property meta = field.getAnnotation(Property.class);
            Object o = field.get(null);

            if(o instanceof SimpleObjectProperty)
                return new PropertyWrapper(meta, (SimpleObjectProperty) o);
        }

        return null;
    }

    public static class PropertyWrapper {
        public Property meta;
        public SimpleObjectProperty property;

        public PropertyWrapper(Property meta, SimpleObjectProperty property) {
            this.meta = meta;
            this.property = property;
        }
    }

}

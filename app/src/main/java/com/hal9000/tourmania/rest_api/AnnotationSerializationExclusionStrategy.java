package com.hal9000.tourmania.rest_api;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class AnnotationSerializationExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(SerializationExclude.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}

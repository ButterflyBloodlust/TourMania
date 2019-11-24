package com.hal9000.tourmania.rest_api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used for excluding class' fields from gson serialization when used with
 * ExclusionStrategy and GsonBuilder().setExclusionStrategies().
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Exclude {
}
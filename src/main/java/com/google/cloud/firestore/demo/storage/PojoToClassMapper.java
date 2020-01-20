package com.google.cloud.firestore.demo.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * as version previous than firestore-client 1.32 is used and following fix is incorporated
 * https://github.com/googleapis/java-firestore/commit/180f5a965ca2ea8b22338d0cc186b3d8d3bb997e
 * we need this workaround to enable updates of POJOs
 */
class PojoToClassMapper {
    private static final Logger logger = LoggerFactory.getLogger(PojoToClassMapper.class);

    static Object map(Object object) {
        try {
            Class<?> aClass = Class.forName("com.google.cloud.firestore.CustomClassMapper");
            Method convertToPlainJavaTypes = aClass.getDeclaredMethod("convertToPlainJavaTypes", Object.class);
            convertToPlainJavaTypes.setAccessible(true);
            Object map = convertToPlainJavaTypes.invoke(null, object);
            convertToPlainJavaTypes.setAccessible(false);
            return map;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("something went terribly wrong", e);
        }
        return null;
    }
}

/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.ticket.registry.support.kryo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class that provides convenience methods for getting and setting field values via reflection.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public class FieldHelper {
    private final Map<String, Field> fieldCache = new HashMap<String, Field>();

    public Object getFieldValue(final Object target, final String fieldName) {
        final Field f = getField(target, fieldName);
        try {
            return f.get(target);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Error getting field value", e);
        }
    }

    public void setFieldValue(final Object target, final String fieldName, final Object value) {
        final Field f = getField(target, fieldName);
        try {
            f.set(target, value);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Error setting field value", e);
        }
    }

    private Field getField(final Object target, final String name) {
        Class<?> clazz = target.getClass();
        final String key = new StringBuilder().append(clazz.getName()).append('.').append(name).toString();
        Field f = fieldCache.get(key);
        while (f == null) {
            try {
                f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                fieldCache.put(key, f);
            } catch (final NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
                if (clazz == null) {
                    throw new IllegalStateException("No such field " + key);
                }
            }
        }
        return f;
    }

}

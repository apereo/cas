/*
  $Id: $

  Copyright (C) 2012 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: $
  Updated: $Date: $
*/
package org.jasig.cas.ticket.registry.support.kryo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Description of FieldHelper.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class FieldHelper {
    private final Map<String, Field> fieldCache = new HashMap<String, Field>();

    public Object getFieldValue(final Object target, final String fieldName) {
        final Field f = getField(target, fieldName);
        try {
            return f.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Error getting field value", e);
        }
    }

    public void setFieldValue(final Object target, final String fieldName, final Object value) {
        final Field f = getField(target, fieldName);
        try {
            f.set(target, value);
        } catch (IllegalAccessException e) {
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
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
                if (clazz == null) {
                    throw new IllegalStateException("No such field " + key);
                }
            }
        }
        return f;
    }

}

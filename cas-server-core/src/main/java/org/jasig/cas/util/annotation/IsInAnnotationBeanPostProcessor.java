/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.annotation;

import java.lang.reflect.Field;

import org.springframework.beans.FatalBeanException;

/**
 * Checks whether a value is in an array of values.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class IsInAnnotationBeanPostProcessor extends
    AbstractAnnotationBeanPostProcessor {

    protected void processField(final Field field, final Object bean,
        final String beanName) throws IllegalAccessException {
        final IsIn isIn = field.getAnnotation(IsIn.class);

        if (isIn != null) {
            final int val = field.getInt(bean);

            for (int i = 0; i < isIn.value().length; i++) {
                if (val == isIn.value()[i]) {
                    return;
                }
            }

            throw new FatalBeanException("field '" + field.getName()
                + "' does not contain a value of '" + isIn.value()
                + "' on bean '" + beanName + "'");
        }
    }
}

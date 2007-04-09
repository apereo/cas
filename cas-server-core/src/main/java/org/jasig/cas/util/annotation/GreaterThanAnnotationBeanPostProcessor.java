/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.annotation;

import java.lang.reflect.Field;

import org.springframework.beans.FatalBeanException;

/**
 * Works in conjunction with the {@link GreaterThan} annotation to ensure that
 * fields have a proper value set.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 * 
 * <p>TODO: make more robust to support things other than int.</p>
 */
public class GreaterThanAnnotationBeanPostProcessor extends
    AbstractAnnotationBeanPostProcessor {

    @Override
    protected void processField(final Field field, final Object bean,
        final String beanName) throws IllegalAccessException {
        final GreaterThan greaterThan = field.getAnnotation(GreaterThan.class);

        if (greaterThan != null) {
            final int value = greaterThan.value();
            final int val = field.getInt(bean);

            if (val <= value) {
                throw new FatalBeanException("value of field \""
                    + field.getName() + "\" must be greater than \"" + value
                    + "\" on bean \"" + beanName + "\"");
            }
        }
    }
}

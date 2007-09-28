/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.springframework.beans.FatalBeanException;

/**
 * Works in conjunction with the {@link NotNull} annotation to ensure that all
 * fields are properly set.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2007/04/09 04:30:31 $
 * @since 3.1
 */
public final class NotNullAnnotationBeanPostProcessor extends
    AbstractAnnotationBeanPostProcessor {

    protected void processField(final Field field, final Annotation annotation,
        final Object bean, String beanName) throws IllegalAccessException {
        if (field.get(bean) == null) {
            throw new FatalBeanException("Field " + field.getName()
                + " cannot be null on bean: " + beanName);
        }
    }

    protected Class< ? extends Annotation> getSupportedAnnotation() {
        return NotNull.class;
    }
}

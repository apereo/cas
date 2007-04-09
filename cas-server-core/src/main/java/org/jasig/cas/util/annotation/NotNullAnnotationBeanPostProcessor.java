/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.annotation;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;

/**
 * Works in conjunction with the {@link NotNull} annotation to ensure that all
 * fields are properly set.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class NotNullAnnotationBeanPostProcessor extends
    AbstractAnnotationBeanPostProcessor {

    private final Log log = LogFactory.getLog(getClass());

    protected void processField(final Field field, final Object bean,
        String beanName) throws IllegalAccessException {
        if (field.getAnnotation(NotNull.class) != null
            && field.get(bean) == null) {
            throw new FatalBeanException("Field " + field.getName()
                + " cannot be null on bean: " + beanName);
        }
    }
}

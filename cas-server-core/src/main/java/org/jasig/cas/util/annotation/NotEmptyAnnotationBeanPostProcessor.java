/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.springframework.beans.FatalBeanException;

/**
 * @author Scott Battaglia
 * @version $Revision: 1.3 $ $Date: 2007/04/24 18:11:45 $
 * @since 3.1
 */
public final class NotEmptyAnnotationBeanPostProcessor extends
    AbstractAnnotationBeanPostProcessor {

    protected void processField(final Field field, final Annotation annotation,
        final Object bean, final String beanName) throws IllegalAccessException {

        final Object obj = field.get(bean);
        
        if (obj == null) {
            throw new FatalBeanException(constructMessage(field, beanName));
        }
        
        if (obj instanceof Collection) {
            final Collection< ? > c = (Collection< ? >) obj;

            if (c.isEmpty()) {
                throw new FatalBeanException(constructMessage(field, beanName));
            }
        }
        
        if (obj.getClass().isArray()) {
            if (Array.getLength(obj) == 0) {
                throw new FatalBeanException(constructMessage(field, beanName));
            }
        }

        if (obj instanceof Map) {
            final Map< ? , ? > m = (Map< ? , ? >) obj;

            if (m.isEmpty()) {
                throw new FatalBeanException(constructMessage(field, beanName));
            }
        }
    }

    protected String constructMessage(final Field field, final String beanName) {
        return "Field '" + field.getName() + "' on bean '" + beanName
            + "' cannot be empty.";

    }

    protected Class<? extends Annotation> getSupportedAnnotation() {
        return NotEmpty.class;
    }
}

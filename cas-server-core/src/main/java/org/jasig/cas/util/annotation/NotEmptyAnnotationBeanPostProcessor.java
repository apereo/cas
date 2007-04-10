/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.annotation;

import java.lang.reflect.Field;
import java.util.Collection;

import org.springframework.beans.FatalBeanException;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public final class NotEmptyAnnotationBeanPostProcessor extends
    AbstractAnnotationBeanPostProcessor {

    protected void processField(final Field field, final Object bean, final String beanName)
        throws IllegalAccessException {
        final NotEmpty annotation = field.getAnnotation(NotEmpty.class);
        
        if (annotation != null) {
            final Object obj = field.get(bean);
            
            if (obj == null) {
                throw new FatalBeanException(constructMessage(field, beanName)); 
            }
            
            if (obj instanceof Collection) {
                final Collection c = (Collection) obj;
                
                if (c.isEmpty()) {
                    throw new FatalBeanException(constructMessage(field, beanName));
                }
            }
        }
        // TODO Auto-generated method stub

    }
    
    protected String constructMessage(final Field field, final String beanName) {
        return "Field '" + field.getName() + "' on bean '" + beanName + "' cannot be empty.";
        
    }

}

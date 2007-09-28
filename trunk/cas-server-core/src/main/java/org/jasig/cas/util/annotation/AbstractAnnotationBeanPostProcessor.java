/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

/**
 * Abstract processor to assist in retrieving fields to check for annotations.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.5 $ $Date: 2007/04/13 20:01:22 $
 * @since 3.1
 */
public abstract class AbstractAnnotationBeanPostProcessor extends
    InstantiationAwareBeanPostProcessorAdapter {

    protected final Log log = LogFactory.getLog(getClass());

    public final Object postProcessBeforeInitialization(final Object bean,
        final String beanName) throws BeansException {

        final List<Field> fields = new ArrayList<Field>();
        final Class< ? > clazz = bean.getClass();
        final Class< ? >[] classes = clazz.getClasses();

        addDeclaredFields(clazz, fields);

        for (int i = 0; i < classes.length; i++) {
            addDeclaredFields(classes[i], fields);
        }

        try {
            for (final Field field : fields) {
                final boolean originalValue = field.isAccessible();
                field.setAccessible(true);
                final Annotation annotation = field
                    .getAnnotation(getSupportedAnnotation());

                if (annotation != null) {
                    processField(field, annotation, bean, beanName);
                }

                field.setAccessible(originalValue);
            }
        } catch (final IllegalAccessException e) {
            log.warn("Could not access field: " + e.getMessage(), e);
        }

        return bean;
    }

    private final void addDeclaredFields(final Class< ? > clazz,
        final List<Field> fields) {
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
    }

    /**
     * @param field the field to check
     * @param bean the bean to check
     * @param beanName the name of the bean.
     * @throws IllegalAccessException
     */
    protected abstract void processField(Field field, Annotation annotation,
        Object bean, String beanName) throws IllegalAccessException;

    protected abstract Class<? extends Annotation> getSupportedAnnotation();
}

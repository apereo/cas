/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;


/**
 * Validates bean dependencies based on the JSR303 javax.validation standard.  This requires an implementation
 * of the Bean Validation Specification to be on the classpath.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3.6
 */
public final class ValidatorBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

    private final Log log = LogFactory.getLog(getClass());

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        log.debug("Starting to validate bean [" + beanName + "].");
        final Set<ConstraintViolation<Object>> constraints = this.validator.validate(bean);

        if (constraints.isEmpty()) {
            log.debug("No validation errors found for bean [" + beanName + "]");
            return bean;
        }

        throw new FatalBeanException(constraints.toString());
    }
}

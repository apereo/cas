/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.util;

import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;

import java.lang.annotation.ElementType;

import javax.validation.Configuration;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * Provides a custom {@link javax.validation.TraversableResolver} that should work in JPA2 environments without the JPA2
 * restrictions (i.e. getters for all properties).
 *
 * @author Scott Battaglia
 * @since 3.4
 *
 */
public final class CustomBeanValidationPostProcessor extends BeanValidationPostProcessor {

    /**
     * Instantiates a new custom bean validation post processor.
     */
    public CustomBeanValidationPostProcessor() {
        final Configuration<?> configuration = Validation.byDefaultProvider().configure();
        configuration.traversableResolver(new TraversableResolver() {

            @Override
            public boolean isReachable(final Object traversableObject, final Node traversableProperty,
                    final Class<?> rootBeanType,
                    final Path pathToTraversableObject, final ElementType elementType) {
                return true;
            }

            @Override
            public boolean isCascadable(final Object traversableObject, final Node traversableProperty,
                    final Class<?> rootBeanType,
                    final Path pathToTraversableObject, final ElementType elementType) {
                return true;
            }
        });

        final Validator validator = configuration.buildValidatorFactory().getValidator();
        setValidator(validator);
    }
}

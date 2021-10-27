/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;

import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import java.util.Locale;

/**
 * Configures the {@link javax.validation.Validator} to check the Spring Messages.
 *
 * @author Scott Battaglia

 * @since 3.4
 */
public final class SpringAwareMessageMessageInterpolator implements MessageInterpolator, MessageSourceAware {

    private MessageInterpolator defaultMessageInterpolator =
            Validation.byDefaultProvider().configure().getDefaultMessageInterpolator();

    private MessageSource messageSource;

    public void setMessageSource(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String interpolate(final String s, final Context context) {
        return interpolate(s, context, LocaleContextHolder.getLocale());
    }

    public String interpolate(final String s, final Context context, final Locale locale) {
        try {
            return this.messageSource.getMessage(s,
                    context.getConstraintDescriptor().getAttributes().values().toArray(
                    new Object[context.getConstraintDescriptor().getAttributes().size()]), locale);
        } catch (final NoSuchMessageException e) {
            return this.defaultMessageInterpolator.interpolate(s, context, locale);
        }
    }
}

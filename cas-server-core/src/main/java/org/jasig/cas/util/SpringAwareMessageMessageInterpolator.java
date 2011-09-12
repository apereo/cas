/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
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
 * @version $Revision$ $Date$
 * @since 3.4
 */
public final class SpringAwareMessageMessageInterpolator implements MessageInterpolator, MessageSourceAware {

    private MessageInterpolator defaultMessageInterpolator = Validation.byDefaultProvider().configure().getDefaultMessageInterpolator();

    private MessageSource messageSource;

    public void setMessageSource(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String interpolate(final String s, final Context context) {
        return interpolate(s, context, LocaleContextHolder.getLocale());
    }

    public String interpolate(final String s, final Context context, final Locale locale) {
        try {
            return this.messageSource.getMessage(s, context.getConstraintDescriptor().getAttributes().values().toArray(new Object[context.getConstraintDescriptor().getAttributes().size()]), locale);
        } catch (final NoSuchMessageException e) {
            return this.defaultMessageInterpolator.interpolate(s, context, locale);
        }
    }
}

package org.apereo.cas.util.spring;

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
public class SpringAwareMessageMessageInterpolator implements MessageInterpolator, MessageSourceAware {

    private final MessageInterpolator defaultMessageInterpolator =
            Validation.byDefaultProvider().configure().getDefaultMessageInterpolator();

    private MessageSource messageSource;

    @Override
    public void setMessageSource(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String interpolate(final String s, final Context context) {
        return interpolate(s, context, LocaleContextHolder.getLocale());
    }

    @Override
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

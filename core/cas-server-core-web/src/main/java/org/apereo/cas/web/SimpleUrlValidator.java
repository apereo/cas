package org.apereo.cas.web;

import org.apache.commons.validator.routines.UrlValidator;

/**
 * Implementation of CAS {@link org.apereo.cas.web.UrlValidator}
 * which delegates requests to a {@link #urlValidator} instance.
 * 
 * @author swoeste
 * @since 5.2.0
 */
public class SimpleUrlValidator implements org.apereo.cas.web.UrlValidator {

    private final UrlValidator urlValidator;

    SimpleUrlValidator(final UrlValidator urlValidator){
        this.urlValidator = urlValidator;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isValid(final String value) {
        return this.urlValidator.isValid(value);
    }

}

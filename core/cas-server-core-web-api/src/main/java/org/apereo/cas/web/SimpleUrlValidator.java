package org.apereo.cas.web;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Implementation of CAS {@link org.apereo.cas.web.UrlValidator}
 * which delegates requests to a {@link #urlValidator} instance.
 *
 * @author swoeste
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class SimpleUrlValidator implements org.apereo.cas.web.UrlValidator {
    private static org.apereo.cas.web.UrlValidator INSTANCE;

    private final UrlValidator urlValidator;
    private final DomainValidator domainValidator;

    /**
     * Gets a static instance to be used internal only.
     *
     * @return the instance
     */
    public static org.apereo.cas.web.UrlValidator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SimpleUrlValidator(UrlValidator.getInstance(), DomainValidator.getInstance());
        }
        return INSTANCE;
    }

    @Override
    public boolean isValid(final String value) {
        return StringUtils.isNotBlank(value) && this.urlValidator.isValid(value);
    }

    @Override
    public boolean isValidDomain(final String value) {
        return StringUtils.isNotBlank(value) && this.domainValidator.isValid(value);
    }
}

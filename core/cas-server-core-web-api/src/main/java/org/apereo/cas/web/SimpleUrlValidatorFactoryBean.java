package org.apereo.cas.web;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.RegexValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.FactoryBean;


/**
 * The factory to build a {@link org.apereo.cas.web.UrlValidator}.
 *
 * @author swoeste
 * @since 5.2.0
 */
public class SimpleUrlValidatorFactoryBean implements FactoryBean<org.apereo.cas.web.UrlValidator> {

    private static final UrlValidator URL_VALIDATOR_ALLOW_LOCAL_URLS = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

    private final boolean allowLocalUrls;
    private final UrlValidator urlValidatorWithRegex;

    public SimpleUrlValidatorFactoryBean(final boolean allowLocalUrls) {
        this(allowLocalUrls, null, true);
    }

    public SimpleUrlValidatorFactoryBean(final boolean allowLocalUrls, final String authorityValidationRegEx,
                                         final boolean authorityValidationRegExCaseSensitive) {
        this.allowLocalUrls = allowLocalUrls;
        this.urlValidatorWithRegex = createUrlValidatorWithRegex(allowLocalUrls, authorityValidationRegEx, authorityValidationRegExCaseSensitive);
    }

    private static UrlValidator createUrlValidatorWithRegex(final boolean allowLocalUrls, final String authorityValidationRegEx,
                                                            final boolean authorityValidationRegExCaseSensitive) {
        if (StringUtils.isEmpty(authorityValidationRegEx)) {
            return null;
        }

        val authorityValidator = new RegexValidator(authorityValidationRegEx, authorityValidationRegExCaseSensitive);
        val options = allowLocalUrls ? UrlValidator.ALLOW_LOCAL_URLS : 0;
        return new UrlValidator(authorityValidator, options);
    }

    @Override
    public org.apereo.cas.web.UrlValidator getObject() {
        return new SimpleUrlValidator(getUrlValidator(), getDomainValidator());
    }

    private UrlValidator getUrlValidator() {
        if (this.urlValidatorWithRegex != null) {
            return urlValidatorWithRegex;
        }

        if (this.allowLocalUrls) {
            return URL_VALIDATOR_ALLOW_LOCAL_URLS;
        }

        return UrlValidator.getInstance();
    }

    public DomainValidator getDomainValidator() {
        return DomainValidator.getInstance(this.allowLocalUrls);
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleUrlValidator.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}

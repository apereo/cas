package org.apereo.cas.web;

import org.apache.commons.validator.routines.DomainValidator;
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

    public SimpleUrlValidatorFactoryBean(final boolean allowLocalUrls) {
        this.allowLocalUrls = allowLocalUrls;
    }

    @Override
    public org.apereo.cas.web.UrlValidator getObject() {
        return new SimpleUrlValidator(getUrlValidator(), getDomainValidator());
    }

    private UrlValidator getUrlValidator() {
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

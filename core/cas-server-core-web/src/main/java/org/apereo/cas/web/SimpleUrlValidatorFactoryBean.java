package org.apereo.cas.web;

import org.apache.commons.validator.routines.UrlValidator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.FactoryBean;

/**
 * The factory to build a {@link org.apereo.cas.web.UrlValidator}.
 * 
 * @author swoeste
 * @since 5.2.0
 */
public class SimpleUrlValidatorFactoryBean implements FactoryBean<org.apereo.cas.web.UrlValidator> {

    private static final UrlValidator URL_VALIDATOR_ALLOW_LOCAL_URLS = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

    private CasConfigurationProperties casProperties;

    /** {@inheritDoc} */
    @Override
    public org.apereo.cas.web.UrlValidator getObject() throws Exception {
        final UrlValidator instance = getUrlValidator();
        return new SimpleUrlValidator(instance);
    }

    private UrlValidator getUrlValidator() {
        if (this.casProperties.getHttpClient().isAllowLocalLogoutUrls()){
            return URL_VALIDATOR_ALLOW_LOCAL_URLS;
        }
        return UrlValidator.getInstance();
    }

    /**
     * @param casProperties the casProperties to set
     */
    public void setCasProperties(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getObjectType() {
        return SimpleUrlValidator.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSingleton() {
        return true;
    }

}

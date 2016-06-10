package org.apereo.cas.support.saml.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.SamlGoogleAppsApplicationContextWrapper;
import org.apereo.cas.support.saml.authentication.principal.GoogleAccountsService;
import org.apereo.cas.support.saml.authentication.principal.GoogleAccountsServiceFactory;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlGoogleAppsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlGoogleAppsConfiguration")
public class SamlGoogleAppsConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("googleSaml20ObjectBuilder")
    private GoogleSaml20ObjectBuilder builder;

    @Bean
    public BaseApplicationContextWrapper samlGoogleAppsApplicationContextWrapper() {
        return new SamlGoogleAppsApplicationContextWrapper();
    }

    @Bean
    @RefreshScope
    public ServiceFactory<GoogleAccountsService> googleAccountsServiceFactory() {
        final GoogleAccountsServiceFactory factory = new GoogleAccountsServiceFactory();
        factory.setKeyAlgorithm(casProperties.getGoogleApps().getKeyAlgorithm());
        factory.setPrivateKeyLocation(casProperties.getGoogleApps().getPrivateKeyLocation());
        factory.setPublicKeyLocation(casProperties.getGoogleApps().getPublicKeyLocation());
        factory.setSkewAllowance(casProperties.getSamlResponse().getSkewAllowance());
        factory.setBuilder(this.builder);
        return factory;
    }

    @Bean
    public GoogleSaml20ObjectBuilder googleSaml20ObjectBuilder() {
        return new GoogleSaml20ObjectBuilder();
    }
}

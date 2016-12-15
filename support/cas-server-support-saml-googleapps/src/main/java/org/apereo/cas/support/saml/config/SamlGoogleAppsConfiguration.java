package org.apereo.cas.support.saml.config;

import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.authentication.principal.GoogleAccountsServiceFactory;
import org.apereo.cas.support.saml.authentication.principal.GoogleAccountsServiceResponseBuilder;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;

/**
 * This is {@link SamlGoogleAppsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlGoogleAppsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlGoogleAppsConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier("defaultArgumentExtractor")
    private ArgumentExtractor argumentExtractor;

    @Autowired
    private CasConfigurationProperties casProperties;

    @PostConstruct
    protected void init() {
        this.argumentExtractor.getServiceFactories().add(0, googleAccountsServiceFactory());
    }

    @Bean
    @RefreshScope
    public ServiceFactory googleAccountsServiceFactory() {
        return new GoogleAccountsServiceFactory(googleSaml20ObjectBuilder());
    }

    @Bean
    public GoogleSaml20ObjectBuilder googleSaml20ObjectBuilder() {
        return new GoogleSaml20ObjectBuilder(openSamlConfigBean);
    }

    @ConditionalOnMissingBean(name = "googleAccountsServiceResponseBuilder")
    @Bean
    @Lazy
    public ResponseBuilder googleAccountsServiceResponseBuilder() {
        final GoogleAccountsServiceResponseBuilder responseBuilder =
                new GoogleAccountsServiceResponseBuilder(casProperties.getGoogleApps().getPrivateKeyLocation(),
                        casProperties.getGoogleApps().getPublicKeyLocation(),
                        casProperties.getGoogleApps().getKeyAlgorithm(),
                        servicesManager,
                        googleSaml20ObjectBuilder());
        responseBuilder.setSkewAllowance(casProperties.getSamlCore().getSkewAllowance());
        return responseBuilder;
    }
}

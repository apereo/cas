package org.apereo.cas.support.saml.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
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
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public BaseApplicationContextWrapper samlGoogleAppsApplicationContextWrapper() {
        final SamlGoogleAppsApplicationContextWrapper w = new SamlGoogleAppsApplicationContextWrapper();
        w.setGoogleAccountsServiceFactory(googleAccountsServiceFactory());
        return w;
    }

    @Bean
    @RefreshScope
    public ServiceFactory<GoogleAccountsService> googleAccountsServiceFactory() {
        final GoogleAccountsServiceFactory factory = new GoogleAccountsServiceFactory();
        factory.setKeyAlgorithm(casProperties.getGoogleApps().getKeyAlgorithm());
        factory.setPrivateKeyLocation(casProperties.getGoogleApps().getPrivateKeyLocation());
        factory.setPublicKeyLocation(casProperties.getGoogleApps().getPublicKeyLocation());
        factory.setSkewAllowance(casProperties.getSamlResponse().getSkewAllowance());
        factory.setBuilder(googleSaml20ObjectBuilder());
        return factory;
    }

    @Bean
    public GoogleSaml20ObjectBuilder googleSaml20ObjectBuilder() {
        final GoogleSaml20ObjectBuilder b = new GoogleSaml20ObjectBuilder();
        b.setConfigBean(openSamlConfigBean);
        return b;
    }
}

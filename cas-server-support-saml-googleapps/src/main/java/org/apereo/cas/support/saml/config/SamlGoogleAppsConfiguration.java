package org.apereo.cas.support.saml.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.authentication.principal.GoogleAccountsServiceFactory;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    @Qualifier("defaultArgumentExtractor")
    private ArgumentExtractor argumentExtractor;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @PostConstruct
    protected void initializeRootApplicationContext() {
        this.argumentExtractor.getServiceFactories().add(0, googleAccountsServiceFactory());
    }

    @Bean
    @RefreshScope
    public ServiceFactory googleAccountsServiceFactory() {
        final GoogleAccountsServiceFactory factory = new GoogleAccountsServiceFactory();
        factory.setKeyAlgorithm(casProperties.getGoogleApps().getKeyAlgorithm());
        factory.setPrivateKeyLocation(casProperties.getGoogleApps().getPrivateKeyLocation());
        factory.setPublicKeyLocation(casProperties.getGoogleApps().getPublicKeyLocation());
        factory.setSkewAllowance(casProperties.getSamlCore().getSkewAllowance());
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

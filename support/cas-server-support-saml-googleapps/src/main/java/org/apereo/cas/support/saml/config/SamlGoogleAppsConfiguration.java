package org.apereo.cas.support.saml.config;

import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.authentication.principal.GoogleAccountsServiceFactory;
import org.apereo.cas.support.saml.authentication.principal.GoogleAccountsServiceResponseBuilder;
import org.apereo.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

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
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ServiceFactoryConfigurer googleAccountsServiceFactoryConfigurer() {
        return () -> CollectionUtils.wrap(googleAccountsServiceFactory());
    }


    @ConditionalOnMissingBean(name = "googleAccountsServiceFactory")
    @Bean
    @RefreshScope
    public ServiceFactory googleAccountsServiceFactory() {
        return new GoogleAccountsServiceFactory(googleSaml20ObjectBuilder());
    }

    @ConditionalOnMissingBean(name = "googleSaml20ObjectBuilder")
    @Bean
    public GoogleSaml20ObjectBuilder googleSaml20ObjectBuilder() {
        return new GoogleSaml20ObjectBuilder(openSamlConfigBean.getObject());
    }

    @ConditionalOnMissingBean(name = "googleAccountsServiceResponseBuilder")
    @Bean
    @Lazy
    public ResponseBuilder googleAccountsServiceResponseBuilder() {
        val gApps = casProperties.getGoogleApps();
        return new GoogleAccountsServiceResponseBuilder(
            gApps.getPrivateKeyLocation(),
            gApps.getPublicKeyLocation(),
            gApps.getKeyAlgorithm(),
            servicesManager.getObject(),
            googleSaml20ObjectBuilder(),
            casProperties.getSamlCore().getSkewAllowance(),
            casProperties.getServer().getPrefix());
    }
}

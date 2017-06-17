package org.apereo.cas.support.wsfederation.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.web.flow.WsFederationAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import java.util.Collection;

/**
 * This is {@link WsFederationAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("wsFederationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WsFederationAuthenticationConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean configBean;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Bean
    @RefreshScope
    public WsFederationHelper wsFederationHelper() {
        final WsFederationHelper h = new WsFederationHelper();
        h.setConfigBean(this.configBean);
        return h;
    }

    @Autowired
    @Bean
    @RefreshScope
    public Action wsFederationAction(@Qualifier("wsFederationConfigurations")
                                     final Collection<WsFederationConfiguration> wsFederationConfigurations) {
        return new WsFederationAction(authenticationSystemSupport,
                centralAuthenticationService,
                wsFederationConfigurations,
                wsFederationHelper(),
                servicesManager);
    }
}

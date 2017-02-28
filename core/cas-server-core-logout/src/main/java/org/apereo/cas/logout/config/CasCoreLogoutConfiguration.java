package org.apereo.cas.logout.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.LogoutManagerImpl;
import org.apereo.cas.logout.LogoutMessageCreator;
import org.apereo.cas.logout.SamlCompliantLogoutMessageCreator;
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.SingleLogoutServiceMessageHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * This is {@link CasCoreLogoutConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreLogoutConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreLogoutConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("authenticationRequestServiceSelectionStrategies")
    private List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies;

    @ConditionalOnMissingBean(name = "defaultSingleLogoutServiceLogoutUrlBuilder")
    @Bean
    public SingleLogoutServiceLogoutUrlBuilder defaultSingleLogoutServiceLogoutUrlBuilder() {
        return new DefaultSingleLogoutServiceLogoutUrlBuilder();
    }

    @ConditionalOnMissingBean(name = "defaultSingleLogoutServiceMessageHandler")
    @Bean
    public SingleLogoutServiceMessageHandler defaultSingleLogoutServiceMessageHandler() {
        return new DefaultSingleLogoutServiceMessageHandler(httpClient,
                logoutBuilder(),
                servicesManager,
                defaultSingleLogoutServiceLogoutUrlBuilder(),
                casProperties.getSlo().isAsynchronous(),
                authenticationRequestServiceSelectionStrategies);
    }

    @ConditionalOnMissingBean(name = "logoutManager")
    @RefreshScope
    @Bean
    public LogoutManager logoutManager() {
        return new LogoutManagerImpl(logoutBuilder(), defaultSingleLogoutServiceMessageHandler(), casProperties.getSlo().isDisabled());
    }

    @ConditionalOnMissingBean(name = "logoutBuilder")
    @Bean
    public LogoutMessageCreator logoutBuilder() {
        return new SamlCompliantLogoutMessageCreator();
    }
}

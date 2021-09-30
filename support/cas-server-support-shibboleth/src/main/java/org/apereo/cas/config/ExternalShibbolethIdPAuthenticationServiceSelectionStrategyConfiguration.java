package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link ExternalShibbolethIdPAuthenticationServiceSelectionStrategyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "externalShibbolethIdPAuthenticationServiceSelectionStrategyConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class ExternalShibbolethIdPAuthenticationServiceSelectionStrategyConfiguration {

    @Autowired
    @ConditionalOnMissingBean(name = "shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationServiceSelectionStrategy shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy(
        final CasConfigurationProperties casProperties,
        @Qualifier("registeredServiceAccessStrategyEnforcer")
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("webApplicationServiceFactory")
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        return new ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy(
            servicesManager,
            webApplicationServiceFactory,
            casProperties.getAuthn().getShibIdp().getServerUrl(),
            registeredServiceAccessStrategyEnforcer);
    }

    @Bean
    @Autowired
    public AuthenticationServiceSelectionStrategyConfigurer shibbolethIdPAuthenticationServiceSelectionStrategyConfigurer(
        @Qualifier("shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy")
        final AuthenticationServiceSelectionStrategy shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy,
        final CasConfigurationProperties casProperties) {
        return plan -> {
            if (StringUtils.isNotBlank(casProperties.getAuthn().getShibIdp().getServerUrl())) {
                plan.registerStrategy(shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy);
            } else {
                LOGGER.warn("Shibboleth IdP url is not specified; External authentication requests by the IdP will not be recognized");
            }
        };
    }

}

package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link ExternalShibbolethIdPAuthenticationServiceSelectionStrategyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("externalShibbolethIdPAuthenticationServiceSelectionStrategyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class ExternalShibbolethIdPAuthenticationServiceSelectionStrategyConfiguration implements AuthenticationServiceSelectionStrategyConfigurer {


    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @ConditionalOnMissingBean(name = "shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy")
    @Bean
    @RefreshScope
    public AuthenticationServiceSelectionStrategy shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy() {
        return new ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy(webApplicationServiceFactory,
                casProperties.getAuthn().getShibIdp().getServerUrl(),
                servicesManager,
                registeredServiceAccessStrategyEnforcer);
    }

    @Override
    public void configureAuthenticationServiceSelectionStrategy(final AuthenticationServiceSelectionPlan plan) {
        if (StringUtils.isNotBlank(casProperties.getAuthn().getShibIdp().getServerUrl())) {
            plan.registerStrategy(shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy());
        } else {
            LOGGER.warn("Shibboleth IdP url is not specified; External authentication requests by the IdP will not be recognized by CAS");
        }
    }
}

package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasShibbolethIdPAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAML)
@AutoConfiguration
public class CasShibbolethIdPAutoConfiguration {

    @ConditionalOnMissingBean(name = "shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationServiceSelectionStrategy shibbolethIdPEntityIdAuthenticationServiceSelectionStrategy(
        final CasConfigurationProperties casProperties,
        @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        return new ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy(
            servicesManager,
            webApplicationServiceFactory,
            casProperties.getAuthn().getShibIdp().getServerUrl(),
            registeredServiceAccessStrategyEnforcer);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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

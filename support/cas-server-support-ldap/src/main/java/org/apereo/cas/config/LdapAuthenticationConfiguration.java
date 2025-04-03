package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.TenantLdapAuthenticationHandlerBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authorization.EndpointLdapAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.core.monitor.LdapSecurityActuatorEndpointsMonitorProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import java.util.stream.Collectors;

/**
 * This is {@link LdapAuthenticationConfiguration} that attempts to create
 * relevant authentication handlers for LDAP.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.LDAP, module = "authentication")
@Configuration(value = "LdapAuthenticationConfiguration", proxyBeanMethods = false)
class LdapAuthenticationConfiguration {

    @Configuration(value = "LdapCoreAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class LdapCoreAuthenticationConfiguration {
        @ConditionalOnMissingBean(name = "ldapPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory ldapPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }
    }

    @Configuration(value = "LdapAuthenticationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class LdapAuthenticationPlanConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "ldapAuthenticationHandlers")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<AuthenticationHandler> ldapAuthenticationHandlers(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("ldapPrincipalFactory")
            final PrincipalFactory ldapPrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val handlers = casProperties.getAuthn().getLdap()
                .stream()
                .filter(LdapUtils::isLdapAuthenticationConfigured)
                .map(prop -> {
                    val handler = LdapUtils.createLdapAuthenticationHandler(prop,
                        applicationContext, servicesManager, ldapPrincipalFactory);
                    handler.setState(prop.getState());
                    LOGGER.info("Created LDAP authentication handler [{}] with state [{}]",
                        handler.getName(), handler.getState());
                    return handler;
                })
                .collect(Collectors.toList());

            return BeanContainer.of(handlers);
        }

        @ConditionalOnMissingBean(name = "ldapAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer ldapAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("ldapAuthenticationHandlers")
            final BeanContainer<AuthenticationHandler> ldapAuthenticationHandlers,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver) {
            return plan -> ldapAuthenticationHandlers.toList().forEach(handler -> {
                LOGGER.info("Registering LDAP authentication for [{}]", handler.getName());
                plan.registerAuthenticationHandlerWithPrincipalResolver(handler, defaultPrincipalResolver);
            });
        }
    }

    @Configuration(value = "LdapSpringSecurityAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class LdapSpringSecurityAuthenticationConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "ldapHttpWebSecurityConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebSecurityConfigurer<HttpSecurity> ldapHttpWebSecurityConfigurer(
            final SecurityProperties securityProperties,
            final CasConfigurationProperties casProperties) {
            return new LdapHttpSecurityCasWebSecurityConfigurer(casProperties, securityProperties);
        }

        @RequiredArgsConstructor
        private static final class LdapHttpSecurityCasWebSecurityConfigurer implements CasWebSecurityConfigurer<HttpSecurity> {
            private final CasConfigurationProperties casProperties;
            private final SecurityProperties securityProperties;

            private EndpointLdapAuthenticationProvider endpointLdapAuthenticationProvider;

            @Override
            public void destroy() {
                FunctionUtils.doIfNotNull(endpointLdapAuthenticationProvider, EndpointLdapAuthenticationProvider::destroy);
            }

            @Override
            @CanIgnoreReturnValue
            public CasWebSecurityConfigurer<HttpSecurity> configure(final HttpSecurity http) {
                val ldap = casProperties.getMonitor().getEndpoints().getLdap();
                if (StringUtils.isNotBlank(ldap.getLdapUrl()) && StringUtils.isNotBlank(ldap.getSearchFilter())) {
                    configureLdapAuthenticationProvider(http, ldap);
                } else {
                    LOGGER.trace("No LDAP url or search filter is defined to enable LDAP authentication");
                }
                return this;
            }

            private void configureLdapAuthenticationProvider(final HttpSecurity http,
                                                             final LdapSecurityActuatorEndpointsMonitorProperties ldap) {
                if (isLdapAuthorizationActive()) {
                    val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(ldap);
                    val authenticator = LdapUtils.newLdaptiveAuthenticator(ldap);
                    this.endpointLdapAuthenticationProvider = new EndpointLdapAuthenticationProvider(ldap,
                        securityProperties, connectionFactory, authenticator);
                    http.authenticationProvider(endpointLdapAuthenticationProvider);
                }
            }

            private boolean isLdapAuthorizationActive() {
                val ldap = casProperties.getMonitor().getEndpoints().getLdap();
                return StringUtils.isNotBlank(ldap.getBaseDn())
                    && StringUtils.isNotBlank(ldap.getLdapUrl())
                    && StringUtils.isNotBlank(ldap.getSearchFilter())
                    && (StringUtils.isNotBlank(ldap.getLdapAuthz().getRoleAttribute())
                    || StringUtils.isNotBlank(ldap.getLdapAuthz().getGroupAttribute()));
            }
        }
    }

    @Configuration(value = "LdapMultitenancyAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Multitenancy)
    static class LdapMultitenancyAuthenticationConfiguration {
        @ConditionalOnMissingBean(name = "ldapMultitenancyAuthenticationPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer ldapMultitenancyAuthenticationPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("ldapPrincipalFactory")
            final PrincipalFactory ldapPrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return plan -> {
                if (casProperties.getMultitenancy().getCore().isEnabled()) {
                    val builder = new TenantLdapAuthenticationHandlerBuilder(applicationContext, ldapPrincipalFactory, servicesManager);
                    plan.registerTenantAuthenticationHandlerBuilder(builder);
                }
            };
        }
    }
}

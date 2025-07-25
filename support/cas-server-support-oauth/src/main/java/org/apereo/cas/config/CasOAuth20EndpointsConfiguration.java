package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AuthorizeEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20CallbackAuthorizeEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20DeviceUserCodeApprovalEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20IntrospectionEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20RevocationEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileEndpointController;
import org.apereo.cas.support.oauth.web.mgmt.OAuth20TokenManagementEndpoint;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.List;

/**
 * This this {@link CasOAuth20EndpointsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OAuth)
@Configuration(value = "CasOAuth20EndpointsConfiguration", proxyBeanMethods = false)
class CasOAuth20EndpointsConfiguration {

    @Configuration(value = "CasOAuth20EndpointControllersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuth20EndpointControllersConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "callbackAuthorizeController")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20CallbackAuthorizeEndpointController callbackAuthorizeController(
            @Qualifier(OAuth20ConfigurationContext.BEAN_NAME)
            final OAuth20ConfigurationContext context) {
            return new OAuth20CallbackAuthorizeEndpointController(context);
        }

        @ConditionalOnMissingBean(name = "introspectionEndpointController")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20IntrospectionEndpointController<OAuth20ConfigurationContext> introspectionEndpointController(
            @Qualifier(OAuth20ConfigurationContext.BEAN_NAME)
            final OAuth20ConfigurationContext context) {
            return new OAuth20IntrospectionEndpointController<>(context);
        }

        @ConditionalOnMissingBean(name = "accessTokenController")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AccessTokenEndpointController<OAuth20ConfigurationContext> accessTokenController(
            @Qualifier("accessTokenGrantAuditableRequestExtractor")
            final AuditableExecution accessTokenGrantAuditableRequestExtractor,
            @Qualifier(OAuth20ConfigurationContext.BEAN_NAME)
            final OAuth20ConfigurationContext context) {
            return new OAuth20AccessTokenEndpointController(context, accessTokenGrantAuditableRequestExtractor);
        }

        @ConditionalOnMissingBean(name = "deviceUserCodeApprovalEndpointController")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20DeviceUserCodeApprovalEndpointController deviceUserCodeApprovalEndpointController(
            @Qualifier(OAuth20ConfigurationContext.BEAN_NAME)
            final OAuth20ConfigurationContext context) {
            return new OAuth20DeviceUserCodeApprovalEndpointController(context);
        }

        @ConditionalOnMissingBean(name = "oauthProfileController")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20UserProfileEndpointController<OAuth20ConfigurationContext> oauthProfileController(
            @Qualifier(OAuth20ConfigurationContext.BEAN_NAME)
            final OAuth20ConfigurationContext context) {
            return new OAuth20UserProfileEndpointController(context);
        }

        @ConditionalOnMissingBean(name = "oauthRevocationController")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20RevocationEndpointController<OAuth20ConfigurationContext> oauthRevocationController(
            @Qualifier(OAuth20ConfigurationContext.BEAN_NAME)
            final OAuth20ConfigurationContext context) {
            return new OAuth20RevocationEndpointController(context);
        }

        @ConditionalOnMissingBean(name = "authorizeController")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizeEndpointController<OAuth20ConfigurationContext> authorizeController(
            @Qualifier(OAuth20ConfigurationContext.BEAN_NAME)
            final OAuth20ConfigurationContext context) {
            return new OAuth20AuthorizeEndpointController(context);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20TokenManagementEndpoint oauth20TokenManagementEndpoint(
            @Qualifier(TicketRegistry.BEAN_NAME)
            final ObjectProvider<TicketRegistry> ticketRegistry,
            @Qualifier(JwtBuilder.ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME)
            final ObjectProvider<JwtBuilder> accessTokenJwtBuilder,
            final CasConfigurationProperties casProperties) {
            return new OAuth20TokenManagementEndpoint(casProperties, ticketRegistry, accessTokenJwtBuilder);
        }
    }

    @Configuration(value = "CasOAuth20EndpointSecurityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasOAuth20EndpointSecurityConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "oauth20ProtocolEndpointConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebSecurityConfigurer<Void> oauth20ProtocolEndpointConfigurer() {
            return new CasWebSecurityConfigurer<>() {
                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(Strings.CI.prependIfMissing(OAuth20Constants.BASE_OAUTH20_URL, "/"));
                }
            };
        }
    }
}

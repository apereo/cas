package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@Configuration(value = "casOAuth20EndpointsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuth20EndpointsConfiguration {

    @Configuration(value = "CasOAuth20EndpointControllersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20EndpointControllersConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "callbackAuthorizeController")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public OAuth20CallbackAuthorizeEndpointController callbackAuthorizeController(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new OAuth20CallbackAuthorizeEndpointController(context);
        }

        @ConditionalOnMissingBean(name = "introspectionEndpointController")
        @Bean
        @Autowired
        public OAuth20IntrospectionEndpointController<OAuth20ConfigurationContext> introspectionEndpointController(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new OAuth20IntrospectionEndpointController<>(context);
        }

        @ConditionalOnMissingBean(name = "accessTokenController")
        @Bean
        @Autowired
        public OAuth20AccessTokenEndpointController accessTokenController(
            @Qualifier("accessTokenGrantAuditableRequestExtractor")
            final AuditableExecution accessTokenGrantAuditableRequestExtractor,
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new OAuth20AccessTokenEndpointController(context, accessTokenGrantAuditableRequestExtractor);
        }


        @ConditionalOnMissingBean(name = "deviceUserCodeApprovalEndpointController")
        @Bean
        @Autowired
        public OAuth20DeviceUserCodeApprovalEndpointController deviceUserCodeApprovalEndpointController(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new OAuth20DeviceUserCodeApprovalEndpointController(context);
        }

        @ConditionalOnMissingBean(name = "oauthProfileController")
        @Bean
        @Autowired
        public OAuth20UserProfileEndpointController oauthProfileController(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new OAuth20UserProfileEndpointController(context);
        }

        @ConditionalOnMissingBean(name = "oauthRevocationController")
        @Bean
        @Autowired
        public OAuth20RevocationEndpointController oauthRevocationController(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new OAuth20RevocationEndpointController(context);
        }

        @ConditionalOnMissingBean(name = "authorizeController")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OAuth20AuthorizeEndpointController authorizeController(
            @Qualifier("oauth20ConfigurationContext")
            final OAuth20ConfigurationContext context) {
            return new OAuth20AuthorizeEndpointController(context);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @Autowired
        public OAuth20TokenManagementEndpoint oauth20TokenManagementEndpoint(
            @Qualifier("accessTokenJwtBuilder")
            final JwtBuilder accessTokenJwtBuilder,
            @Qualifier(CentralAuthenticationService.BEAN_NAME)
            final CentralAuthenticationService centralAuthenticationService,
            final CasConfigurationProperties casProperties) {
            return new OAuth20TokenManagementEndpoint(casProperties,
                centralAuthenticationService, accessTokenJwtBuilder);
        }

    }

    @Configuration(value = "CasOAuth20EndpointSecurityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasOAuth20EndpointSecurityConfiguration {
        @Bean
        public ProtocolEndpointWebSecurityConfigurer<Void> oauth20ProtocolEndpointConfigurer() {
            return new ProtocolEndpointWebSecurityConfigurer<>() {
                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(StringUtils.prependIfMissing(OAuth20Constants.BASE_OAUTH20_URL, "/"));
                }
            };
        }
    }
}

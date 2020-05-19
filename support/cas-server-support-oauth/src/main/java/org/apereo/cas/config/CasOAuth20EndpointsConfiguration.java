package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This this {@link CasOAuth20EndpointsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Configuration(value = "casOAuth20EndpointsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOAuth20EndpointsConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("accessTokenJwtBuilder")
    private ObjectProvider<JwtBuilder> accessTokenJwtBuilder;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("accessTokenGrantAuditableRequestExtractor")
    private ObjectProvider<AuditableExecution> accessTokenGrantAuditableRequestExtractor;

    @Bean
    @ConditionalOnMissingBean(name = "callbackAuthorizeController")
    @RefreshScope
    @Autowired
    public OAuth20CallbackAuthorizeEndpointController callbackAuthorizeController(
        @Qualifier("oauth20ConfigurationContext") final OAuth20ConfigurationContext context) {
        return new OAuth20CallbackAuthorizeEndpointController(context);
    }

    @ConditionalOnMissingBean(name = "introspectionEndpointController")
    @Bean
    @Autowired
    public OAuth20IntrospectionEndpointController introspectionEndpointController(
        @Qualifier("oauth20ConfigurationContext") final OAuth20ConfigurationContext context) {
        return new OAuth20IntrospectionEndpointController(context);
    }

    @ConditionalOnMissingBean(name = "accessTokenController")
    @Bean
    @Autowired
    public OAuth20AccessTokenEndpointController accessTokenController(
        @Qualifier("oauth20ConfigurationContext") final OAuth20ConfigurationContext context) {
        return new OAuth20AccessTokenEndpointController(context, accessTokenGrantAuditableRequestExtractor.getObject());
    }


    @ConditionalOnMissingBean(name = "deviceUserCodeApprovalEndpointController")
    @Bean
    @Autowired
    public OAuth20DeviceUserCodeApprovalEndpointController deviceUserCodeApprovalEndpointController(
        @Qualifier("oauth20ConfigurationContext") final OAuth20ConfigurationContext context) {
        return new OAuth20DeviceUserCodeApprovalEndpointController(context);
    }


    @ConditionalOnMissingBean(name = "profileController")
    @Bean
    @Autowired
    public OAuth20UserProfileEndpointController profileController(
        @Qualifier("oauth20ConfigurationContext") final OAuth20ConfigurationContext context) {
        return new OAuth20UserProfileEndpointController(context);
    }

    @ConditionalOnMissingBean(name = "oauthRevocationController")
    @Bean
    @Autowired
    public OAuth20RevocationEndpointController oauthRevocationController(
        @Qualifier("oauth20ConfigurationContext") final OAuth20ConfigurationContext context) {
        return new OAuth20RevocationEndpointController(context);
    }


    @ConditionalOnMissingBean(name = "authorizeController")
    @Bean
    @RefreshScope
    public OAuth20AuthorizeEndpointController authorizeController(
        @Qualifier("oauth20ConfigurationContext") final OAuth20ConfigurationContext context) {
        return new OAuth20AuthorizeEndpointController(context);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    public OAuth20TokenManagementEndpoint oAuth20TokenManagementEndpoint() {
        return new OAuth20TokenManagementEndpoint(casProperties,
            ticketRegistry.getObject(), accessTokenJwtBuilder.getObject());
    }

}

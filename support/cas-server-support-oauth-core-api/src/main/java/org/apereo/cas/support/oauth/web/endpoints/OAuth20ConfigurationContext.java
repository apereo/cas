package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20InvalidAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionResponseGenerator;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.device.OAuth20DeviceToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.TaskScheduler;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link OAuth20ConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@SuperBuilder
public class OAuth20ConfigurationContext {
    /**
     * Default bean name.
     */
    public static final String BEAN_NAME = "oauth20ConfigurationContext";

    private final ConfigurableApplicationContext applicationContext;

    private final ServicesManager servicesManager;

    private final TicketFactory ticketFactory;

    private final TicketRegistry ticketRegistry;

    private final PrincipalFactory principalFactory;

    private final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    private final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    private final CasConfigurationProperties casProperties;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final OAuth20TokenGenerator accessTokenGenerator;

    private final JwtBuilder accessTokenJwtBuilder;

    private final OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator;

    private final ObjectProvider<List<OAuth20TokenRequestValidator>> accessTokenGrantRequestValidators;

    private final ExpirationPolicyBuilder<OAuth20DeviceToken> deviceTokenExpirationPolicy;

    private final ConsentApprovalViewResolver consentApprovalViewResolver;

    private final OAuth20CasAuthenticationBuilder authenticationBuilder;

    private final ObjectProvider<List<OAuth20AuthorizationResponseBuilder>> oauthAuthorizationResponseBuilders;

    private final OAuth20InvalidAuthorizationResponseBuilder oauthInvalidAuthorizationResponseBuilder;

    private final ObjectProvider<List<OAuth20AuthorizationRequestValidator>> oauthRequestValidators;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final Config oauthConfig;

    private final OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver;

    private final CentralAuthenticationService centralAuthenticationService;

    private final OAuth20UserProfileViewRenderer userProfileViewRenderer;

    private final OAuth20UserProfileDataCreator userProfileDataCreator;

    private final StringSerializer clientRegistrationRequestSerializer;

    private final RandomStringGenerator clientIdGenerator;

    private final RandomStringGenerator clientSecretGenerator;

    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    private final SessionStore sessionStore;

    private final CipherExecutor<Serializable, String> registeredServiceCipherExecutor;

    private final OAuth20TokenSigningAndEncryptionService idTokenSigningAndEncryptionService;

    private final CasCookieBuilder oauthDistributedSessionCookieGenerator;

    private final OAuth20RequestParameterResolver requestParameterResolver;

    private final OAuth20ClientSecretValidator clientSecretValidator;

    private final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    private final ArgumentExtractor argumentExtractor;

    private final AttributeDefinitionStore attributeDefinitionStore;

    private final List<OAuth20IntrospectionResponseGenerator> introspectionResponseGenerator;

    private final PrincipalResolver principalResolver;

    private final TaskScheduler taskScheduler;

    private final CommunicationsManager communicationsManager;

    private final CipherExecutor<byte[], byte[]> webflowCipherExecutor;

    private final HttpClient httpClient;

    private final TenantExtractor tenantExtractor;

    private final MessageSource messageSource;
    
    /**
     * Gets ticket granting ticket.
     *
     * @param context the context
     * @return the ticket granting ticket
     */
    public TicketGrantingTicket fetchTicketGrantingTicketFrom(final JEEContext context) {
        val ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
            getTicketGrantingTicketCookieGenerator(),
            getTicketRegistry(), context.getNativeRequest());
        if (!ticketGrantingTicketCookieGenerator.containsCookie(context.getNativeRequest())) {
            return Optional.ofNullable(ticketGrantingTicket)
                .orElseGet(() -> {
                    val manager = new ProfileManager(context, getSessionStore());
                    return manager.getProfile()
                        .map(profile -> profile.getAttribute(TicketGrantingTicket.class.getName()))
                        .map(ticketId -> ticketRegistry.getTicket(ticketId.toString(), TicketGrantingTicket.class))
                        .orElse(null);
                });
        }
        return ticketGrantingTicket;
    }
}

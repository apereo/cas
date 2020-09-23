package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceToken;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.core.io.ResourceLoader;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * This is {@link OAuth20ConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@Builder
public class OAuth20ConfigurationContext {
    private final ServicesManager servicesManager;

    private final TicketRegistry ticketRegistry;

    private final OAuth20AccessTokenFactory accessTokenFactory;

    private final PrincipalFactory principalFactory;

    private final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    private final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    private final CasConfigurationProperties casProperties;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final OAuth20TokenGenerator accessTokenGenerator;

    private final JwtBuilder accessTokenJwtBuilder;

    private final OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator;

    private final ExpirationPolicyBuilder<OAuth20AccessToken> accessTokenExpirationPolicy;

    private final Collection<OAuth20TokenRequestValidator> accessTokenGrantRequestValidators;

    private final ExpirationPolicyBuilder<OAuth20DeviceToken> deviceTokenExpirationPolicy;

    private final OAuth20CodeFactory oAuthCodeFactory;

    private final ConsentApprovalViewResolver consentApprovalViewResolver;

    private final OAuth20CasAuthenticationBuilder authenticationBuilder;

    private final Set<OAuth20AuthorizationResponseBuilder> oauthAuthorizationResponseBuilders;

    private final Set<OAuth20AuthorizationRequestValidator> oauthRequestValidators;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final Config oauthConfig;

    private final OAuth20DeviceTokenFactory deviceTokenFactory;

    private final OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver;

    private final CentralAuthenticationService centralAuthenticationService;

    private final OAuth20UserProfileViewRenderer userProfileViewRenderer;

    private final OAuth20UserProfileDataCreator userProfileDataCreator;

    private final StringSerializer clientRegistrationRequestSerializer;

    private final RandomStringGenerator clientIdGenerator;

    private final RandomStringGenerator clientSecretGenerator;

    private final ResourceLoader resourceLoader;

    private OAuth20TokenSigningAndEncryptionService idTokenSigningAndEncryptionService;

    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    private final SessionStore<JEEContext> sessionStore;

    private final CipherExecutor<Serializable, String> registeredServiceCipherExecutor;

    private final CasCookieBuilder oauthDistributedSessionCookieGenerator;
}

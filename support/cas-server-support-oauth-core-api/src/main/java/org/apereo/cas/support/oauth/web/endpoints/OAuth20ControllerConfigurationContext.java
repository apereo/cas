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
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.OAuthTokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.device.DeviceTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.pac4j.core.config.Config;
import org.springframework.core.io.ResourceLoader;

import java.util.Collection;
import java.util.Set;

/**
 * This is {@link OAuth20ControllerConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@Builder
public class OAuth20ControllerConfigurationContext {
    private final ServicesManager servicesManager;

    private final TicketRegistry ticketRegistry;

    private final AccessTokenFactory accessTokenFactory;

    private final PrincipalFactory principalFactory;

    private final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    private final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    private final CasConfigurationProperties casProperties;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final OAuth20TokenGenerator accessTokenGenerator;

    private final OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator;

    private final ExpirationPolicy accessTokenExpirationPolicy;

    private final Collection<OAuth20TokenRequestValidator> accessTokenGrantRequestValidators;

    private final ExpirationPolicy deviceTokenExpirationPolicy;

    private final AuditableExecution accessTokenGrantAuditableRequestExtractor;

    private final OAuthCodeFactory oAuthCodeFactory;

    private final ConsentApprovalViewResolver consentApprovalViewResolver;

    private final OAuth20CasAuthenticationBuilder authenticationBuilder;

    private final Set<OAuth20AuthorizationResponseBuilder> oauthAuthorizationResponseBuilders;

    private final Set<OAuth20AuthorizationRequestValidator> oauthRequestValidators;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final Config oauthConfig;

    private final DeviceTokenFactory deviceTokenFactory;

    private final OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver;

    private final CentralAuthenticationService centralAuthenticationService;

    private final OAuth20UserProfileViewRenderer userProfileViewRenderer;

    private final OAuth20UserProfileDataCreator userProfileDataCreator;

    private final StringSerializer clientRegistrationRequestSerializer;

    private final RandomStringGenerator clientIdGenerator;

    private final RandomStringGenerator clientSecretGenerator;

    private final ResourceLoader resourceLoader;

    private final OAuthTokenSigningAndEncryptionService idTokenSigningAndEncryptionService;

    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;
}

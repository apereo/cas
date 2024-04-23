package org.apereo.cas.oidc.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequest;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequestFactory;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20ConsentApprovalViewResolver;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;

import java.util.HashSet;
import java.util.Map;

/**
 * This is {@link OidcConsentApprovalViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OidcConsentApprovalViewResolver extends OAuth20ConsentApprovalViewResolver {
    private final TicketRegistry ticketRegistry;

    private final TicketFactory ticketFactory;

    private final OAuth20RequestParameterResolver oauthRequestParameterResolver;

    public OidcConsentApprovalViewResolver(final CasConfigurationProperties casProperties,
                                           final SessionStore sessionStore,
                                           final TicketRegistry ticketRegistry,
                                           final TicketFactory ticketFactory,
                                           final OAuth20RequestParameterResolver oauthRequestParameterResolver) {
        super(casProperties, sessionStore);
        this.ticketRegistry = ticketRegistry;
        this.ticketFactory = ticketFactory;
        this.oauthRequestParameterResolver = oauthRequestParameterResolver;
    }

    @Override
    protected boolean isConsentApprovalBypassed(final WebContext context, final OAuthRegisteredService service) {
        if (service instanceof OidcRegisteredService) {
            if (context.getRequestURL().endsWith(OidcConstants.PUSHED_AUTHORIZE_URL)) {
                LOGGER.trace("Consent approval is bypassed for pushed authorization requests");
                return true;
            }
            val prompts = oauthRequestParameterResolver.resolveSupportedPromptValues(context);
            if (prompts.contains(OidcConstants.PROMPT_CONSENT)) {
                return false;
            }
        }
        return super.isConsentApprovalBypassed(context, service);
    }

    @Override
    protected String getApprovalViewName() {
        return OidcConstants.CONFIRM_VIEW;
    }

    @Override
    protected void prepareApprovalViewModel(final Map<String, Object> model,
                                            final WebContext webContext,
                                            final OAuthRegisteredService registeredService) throws Exception {
        super.prepareApprovalViewModel(model, webContext, registeredService);
        if (registeredService instanceof final OidcRegisteredService oidcRegisteredService) {

            val dynamicRegistrationPropName = RegisteredServiceProperties.OIDC_DYNAMIC_CLIENT_REGISTRATION.getPropertyName();
            if (oidcRegisteredService.getProperties().containsKey(dynamicRegistrationPropName)) {
                val dynamic = oidcRegisteredService.getProperties().get(dynamicRegistrationPropName).getBooleanValue();
                model.put("dynamic", dynamic);
                model.put("dynamicTime", oidcRegisteredService.getProperties()
                    .get(RegisteredServiceProperties.OIDC_DYNAMIC_CLIENT_REGISTRATION_DATE.getPropertyName()).getValue(String.class));
            }
            val supportedScopes = new HashSet<>(casProperties.getAuthn().getOidc().getDiscovery().getScopes());
            supportedScopes.retainAll(oidcRegisteredService.getScopes());

            val requestedScopes = oauthRequestParameterResolver.resolveRequestedScopes(webContext);
            val userInfoClaims = oauthRequestParameterResolver.resolveUserInfoRequestClaims(webContext);
            webContext.getRequestParameter(OidcConstants.REQUEST_URI).ifPresent(Unchecked.consumer(uri -> {
                val authzRequest = ticketRegistry.getTicket(uri, OidcPushedAuthorizationRequest.class);
                val uriFactory = (OidcPushedAuthorizationRequestFactory) ticketFactory.get(OidcPushedAuthorizationRequest.class);
                val holder = uriFactory.toAccessTokenRequest(authzRequest);
                userInfoClaims.addAll(holder.getClaims().keySet());
                requestedScopes.addAll(holder.getScopes());
            }));
            supportedScopes.retainAll(requestedScopes);
            supportedScopes.add(OidcConstants.StandardScopes.OPENID.getScope());
            model.put("scopes", supportedScopes);
            model.put("userInfoClaims", userInfoClaims);
        }
    }
}

package org.apereo.cas.oidc.web;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequest;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequestFactory;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.views.OAuth20ConsentApprovalViewResolver;

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
    private final CentralAuthenticationService centralAuthenticationService;

    public OidcConsentApprovalViewResolver(final CasConfigurationProperties casProperties,
                                           final SessionStore sessionStore,
                                           final CentralAuthenticationService centralAuthenticationService) {
        super(casProperties, sessionStore);
        this.centralAuthenticationService = centralAuthenticationService;
    }

    @Override
    protected boolean isConsentApprovalBypassed(final WebContext context, final OAuthRegisteredService service) {
        if (service instanceof OidcRegisteredService) {
            if (context.getRequestURL().endsWith(OidcConstants.PUSHED_AUTHORIZE_URL)) {
                LOGGER.trace("Consent approval is bypassed for pushed authorization requests");
                return true;
            }
            val url = context.getFullRequestURL();
            val prompts = OidcRequestSupport.getOidcPromptFromAuthorizationRequest(url);
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
                                            final OAuthRegisteredService svc) throws Exception {
        super.prepareApprovalViewModel(model, webContext, svc);
        if (svc instanceof OidcRegisteredService) {
            val oidcRegisteredService = (OidcRegisteredService) svc;
            model.put("dynamic", oidcRegisteredService.isDynamicallyRegistered());
            model.put("dynamicTime", oidcRegisteredService.getDynamicRegistrationDateTime());
            val supportedScopes = new HashSet<>(casProperties.getAuthn().getOidc().getDiscovery().getScopes());
            supportedScopes.retainAll(oidcRegisteredService.getScopes());

            val requestedScopes = OAuth20Utils.getRequestedScopes(webContext);
            val userInfoClaims = OAuth20Utils.parseUserInfoRequestClaims(webContext);
            webContext.getRequestParameter(OidcConstants.REQUEST_URI).ifPresent(Unchecked.consumer(uri -> {
                val authzRequest = centralAuthenticationService.getTicket(uri, OidcPushedAuthorizationRequest.class);
                val uriFactory = (OidcPushedAuthorizationRequestFactory) centralAuthenticationService.getTicketFactory().get(OidcPushedAuthorizationRequest.class);
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

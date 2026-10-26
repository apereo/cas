package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.dynareg.OidcClientIdMetadataDocumentResolver;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequest;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequestFactory;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20ConsentApprovalViewResolver;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;

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
    
    public OidcConsentApprovalViewResolver(final CasConfigurationProperties casProperties,
                                           final SessionStore sessionStore,
                                           final TicketRegistry ticketRegistry,
                                           final TicketFactory ticketFactory,
                                           final OAuth20RequestParameterResolver oauthRequestParameterResolver) {
        super(casProperties, sessionStore, oauthRequestParameterResolver);
        this.ticketRegistry = ticketRegistry;
        this.ticketFactory = ticketFactory;
    }

    /**
     * Consent is bypassed for pushed authorization requests. With {@code prompt=consent} the end-user must consent
     * even when the service or CAS bypasses the approval prompt, so only the approval just submitted on the consent
     * view lets the request through; the callback URL of that view still carries {@code prompt=consent}.
     *
     * @param context the context
     * @param service the service
     * @return true if consent approval is bypassed
     */
    @Override
    protected boolean isConsentApprovalBypassed(final WebContext context, final OAuthRegisteredService service) {
        if (service instanceof final OidcRegisteredService oidcService) {
            if (context.getRequestURL().endsWith(OidcConstants.PUSHED_AUTHORIZE_URL)) {
                LOGGER.trace("Consent approval is bypassed for pushed authorization requests");
                return true;
            }
            val prompts = oauthRequestParameterResolver.resolveSupportedPromptValues(context);
            if (prompts.contains(OidcConstants.PROMPT_CONSENT)
                || OidcClientIdMetadataDocumentResolver.isClientIdMetadataDocumentClient(oidcService)) {
                return isConsentApprovalSubmitted(context, service);
            }
        }
        return super.isConsentApprovalBypassed(context, service);
    }

    /**
     * Clients known from their client ID metadata document have not been reviewed by anyone, so the consent view names
     * the host of the client id and of the redirect URI, which the client cannot fake, next to the name the client chose,
     * and warns when the redirect URI is on the person's own computer: any local program can claim such an address.
     *
     * @param model             the model
     * @param context           the context
     * @param registeredService the client
     */
    protected void prepareClientIdMetadataDocumentModel(final Map<String, Object> model, final WebContext context,
                                                        final OidcRegisteredService registeredService) {
        model.put("clientIdMetadataDocument", Boolean.TRUE);
        model.put("clientIdHost", URI.create(registeredService.getClientId()).getHost());
        context.getRequestParameter(OAuth20Constants.REDIRECT_URI)
            .map(uri -> FunctionUtils.doAndHandle(() -> URI.create(uri).getHost()))
            .ifPresent(host -> {
                model.put("redirectHost", host);
                model.put("loopbackRedirect", OidcClientIdMetadataDocumentResolver.LOOPBACK_HOSTS.contains(host));
            });
    }

    @Override
    protected String getApprovalViewName() {
        return OidcConstants.CONFIRM_VIEW;
    }

    @Override
    protected void prepareApprovalViewModel(final Map<String, Object> model,
                                            final WebContext context,
                                            final OAuthRegisteredService service)
            throws Exception {
        super.prepareApprovalViewModel(model, context, service);
        if (service instanceof final OidcRegisteredService oidcRegisteredService) {

            val dynamicRegistrationPropName = RegisteredServiceProperties.OIDC_DYNAMIC_CLIENT_REGISTRATION.getPropertyName();
            if (oidcRegisteredService.getProperties().containsKey(dynamicRegistrationPropName)) {
                val dynamic = oidcRegisteredService.getProperties().get(dynamicRegistrationPropName).getBooleanValue();
                model.put("dynamic", dynamic);
                model.put("dynamicTime", oidcRegisteredService.getProperties()
                    .get(RegisteredServiceProperties.OIDC_DYNAMIC_CLIENT_REGISTRATION_DATE.getPropertyName()).getValue(String.class));
            }
            if (OidcClientIdMetadataDocumentResolver.isClientIdMetadataDocumentClient(oidcRegisteredService)) {
                prepareClientIdMetadataDocumentModel(model, context, oidcRegisteredService);
            }
            val supportedScopes = new HashSet<>(casProperties.getAuthn().getOidc().getDiscovery().getScopes());
            supportedScopes.retainAll(oidcRegisteredService.getScopes());

            val requestedScopes = oauthRequestParameterResolver.resolveRequestedScopes(context);
            val userInfoClaims = oauthRequestParameterResolver.resolveUserInfoRequestClaims(context);
            context.getRequestParameter(OidcConstants.REQUEST_URI).ifPresent(Unchecked.consumer(uri -> {
                val authzRequest = ticketRegistry.getTicket(uri, OidcPushedAuthorizationRequest.class);
                val uriFactory = (OidcPushedAuthorizationRequestFactory) ticketFactory.get(OidcPushedAuthorizationRequest.class);
                val holder = uriFactory.toAccessTokenRequest(authzRequest);
                userInfoClaims.addAll(holder.getClaims().keySet());
                requestedScopes.addAll(holder.getScopes());
                val resources = new TreeSet<>(holder.getResources());
                resources.addAll(resolveRequestedResources(context));
                model.put("resources", resources);
            }));
            supportedScopes.retainAll(requestedScopes);
            supportedScopes.add(OidcConstants.StandardScopes.OPENID.getScope());
            model.put("scopes", supportedScopes);
            model.put("userInfoClaims", userInfoClaims);
        }
    }
    
}

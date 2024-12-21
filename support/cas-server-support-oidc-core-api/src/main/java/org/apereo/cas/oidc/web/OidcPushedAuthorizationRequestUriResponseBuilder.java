package org.apereo.cas.oidc.web;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequest;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequestFactory;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.callback.BaseOAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.WebContext;
import org.pac4j.jee.context.JEEContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Optional;

/**
 * This is {@link OidcPushedAuthorizationRequestUriResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class OidcPushedAuthorizationRequestUriResponseBuilder extends BaseOAuth20AuthorizationResponseBuilder<OidcConfigurationContext> {
    public OidcPushedAuthorizationRequestUriResponseBuilder(
        final OidcConfigurationContext configurationContext,
        final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(configurationContext, authorizationModelAndViewBuilder);
    }

    @Override
    @Audit(action = AuditableActions.OAUTH2_AUTHORIZATION_RESPONSE,
        actionResolverName = AuditActionResolvers.OAUTH2_AUTHORIZATION_RESPONSE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OAUTH2_AUTHORIZATION_RESPONSE_RESOURCE_RESOLVER)
    public ModelAndView build(final AccessTokenRequestContext holder) throws Throwable {
        val factory = (OidcPushedAuthorizationRequestFactory) configurationContext.getTicketFactory().get(OidcPushedAuthorizationRequest.class);
        val uri = factory.create(holder);
        LOGGER.debug("Generated pushed authorization URI code: [{}]", uri);
        configurationContext.getTicketRegistry().addTicket(uri);
        val parameters = new HashMap<String, String>();
        parameters.put(OAuth20Constants.EXPIRES_IN, String.valueOf(uri.getExpirationPolicy().getTimeToLive()));
        parameters.put(OidcConstants.REQUEST_URI, uri.getId());
        LOGGER.debug("Pushed authorization request verification successful for client [{}] with redirect uri [{}]", holder.getClientId(), holder.getRedirectUri());
        return authorizationModelAndViewBuilder.build(holder.getRegisteredService(), holder.getResponseMode(),
            holder.getRedirectUri(), parameters);
    }

    @Override
    public Optional<OAuth20AuthorizationRequest.OAuth20AuthorizationRequestBuilder> toAuthorizationRequest(
        final WebContext context,
        final Authentication authentication,
        final Service service,
        final OAuthRegisteredService registeredService) {

        val requestUri = context.getRequestParameter(OidcConstants.REQUEST_URI);
        if (context.getRequestURL().endsWith(OidcConstants.AUTHORIZE_URL) && requestUri.isEmpty()) {
            return Optional.empty();
        }

        val authzRequestBuilder = super.toAuthorizationRequest(context, authentication, service, registeredService).orElseThrow();
        return requestUri
            .map(Unchecked.function(uri -> {
                val factory = (OidcPushedAuthorizationRequestFactory) configurationContext.getTicketFactory().get(OidcPushedAuthorizationRequest.class);
                val pushAuthzTicket = configurationContext.getTicketRegistry().getTicket(uri, OidcPushedAuthorizationRequest.class);
                FunctionUtils.doIf(pushAuthzTicket.isExpired(), Unchecked.consumer(r -> configurationContext.getTicketRegistry().deleteTicket(pushAuthzTicket)),
                    Unchecked.consumer(r -> configurationContext.getTicketRegistry().updateTicket(pushAuthzTicket))).accept(pushAuthzTicket);
                val tgt = configurationContext.fetchTicketGrantingTicketFrom((JEEContext) context);

                val pushAuthzAuthentication = DefaultAuthenticationBuilder.newInstance(tgt.getAuthentication())
                    .mergeAttributes(pushAuthzTicket.getAuthentication().getAttributes())
                    .build();
                val tokenRequest = factory.toAccessTokenRequest(pushAuthzTicket)
                    .withTicketGrantingTicket(tgt)
                    .withAuthentication(pushAuthzAuthentication);
                pushAuthzTicket.update();
                configurationContext.getTicketRegistry().updateTicket(pushAuthzTicket);

                val value = authzRequestBuilder.accessTokenRequest(tokenRequest)
                    .responseType(tokenRequest.getResponseType().getType())
                    .clientId(tokenRequest.getClientId())
                    .grantType(tokenRequest.getGrantType().getType());
                return Optional.of(value);
            }))
            .orElseGet(() -> Optional.of(authzRequestBuilder.singleSignOnSessionRequired(!context.getRequestURL().endsWith(OidcConstants.PUSHED_AUTHORIZE_URL))));
    }

    @Override
    public boolean supports(final OAuth20AuthorizationRequest context) {
        return context.getUrl().endsWith(OidcConstants.PUSHED_AUTHORIZE_URL)
               && StringUtils.isNotBlank(context.getClientId());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

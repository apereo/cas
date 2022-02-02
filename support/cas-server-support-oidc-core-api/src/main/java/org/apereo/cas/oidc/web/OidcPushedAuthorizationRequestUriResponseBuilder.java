package org.apereo.cas.oidc.web;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequest;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequestFactory;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.callback.BaseOAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
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
    public ModelAndView build(final AccessTokenRequestContext holder) throws Exception {
        val factory = (OidcPushedAuthorizationRequestFactory) configurationContext.getTicketFactory().get(OidcPushedAuthorizationRequest.class);
        val uri = factory.create(holder);
        LOGGER.debug("Generated pushed authorization URI code: [{}]", uri);
        configurationContext.getTicketRegistry().addTicket(uri);
        val parameters = new HashMap<String, String>();
        parameters.put(OidcConstants.EXPIRES_IN, String.valueOf(uri.getExpirationPolicy().getTimeToLive()));
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

        val builder = super.toAuthorizationRequest(context, authentication, service, registeredService).get();
        return requestUri
            .map(Unchecked.function(uri -> {
                val cas = configurationContext.getCentralAuthenticationService();
                val factory = (OidcPushedAuthorizationRequestFactory) cas.getTicketFactory().get(OidcPushedAuthorizationRequest.class);
                val request = cas.getTicket(uri, OidcPushedAuthorizationRequest.class);
                val tokenRequest = factory.toAccessTokenRequest(request);
                request.update();
                FunctionUtils.doIf(request.isExpired(), Unchecked.consumer(r -> cas.deleteTicket(request)),
                    Unchecked.consumer(r -> cas.updateTicket(request))).accept(request);
                val tgt = configurationContext.fetchTicketGrantingTicketFrom((JEEContext) context);
                tokenRequest.setTicketGrantingTicket(tgt);
                return Optional.of(builder.accessTokenRequest(tokenRequest)
                    .responseType(tokenRequest.getResponseType().getType())
                    .clientId(tokenRequest.getClientId())
                    .grantType(tokenRequest.getGrantType().getType()));
            }))
            .orElseGet(() -> Optional.of(builder.singleSignOnSessionRequired(!context.getRequestURL().endsWith(OidcConstants.PUSHED_AUTHORIZE_URL))));
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

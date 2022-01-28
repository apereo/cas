package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationRequest;
import org.apereo.cas.oidc.ticket.OidcPushedAuthorizationUriFactory;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.support.oauth.web.response.callback.BaseOAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;

/**
 * This is {@link OidcPushedAuthorizationRequestResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class OidcPushedAuthorizationRequestResponseBuilder extends BaseOAuth20AuthorizationResponseBuilder<OidcConfigurationContext> {
    public OidcPushedAuthorizationRequestResponseBuilder(
        final OidcConfigurationContext configurationContext,
        final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(configurationContext, authorizationModelAndViewBuilder);
    }

    @Override
    public ModelAndView build(final WebContext context, final String clientId,
                              final AccessTokenRequestDataHolder holder) throws Exception {
        val redirectUri = OAuth20Utils.getRequestParameter(context, OAuth20Constants.REDIRECT_URI)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
        context.getRequestParameters().keySet()
            .forEach(key -> context.getRequestParameter(key).ifPresent(value -> holder.getParameters().put(key, value)));
        val factory = (OidcPushedAuthorizationUriFactory) configurationContext.getTicketFactory().get(OidcPushedAuthorizationRequest.class);
        val uri = factory.create(holder);
        LOGGER.debug("Generated pushed authorization URI code: [{}]", uri);

        configurationContext.getTicketRegistry().addTicket(uri);
        val parameters = new HashMap<String, String>();
        parameters.put(OidcConstants.EXPIRES_IN, String.valueOf(uri.getExpirationPolicy().getTimeToLive()));
        parameters.put(OidcConstants.REQUEST_URI, uri.getId());
        LOGGER.debug("Pushed authorization request verification successful for client [{}] with redirect uri [{}]", clientId, redirectUri);
        return authorizationModelAndViewBuilder.build(context, holder.getRegisteredService(), redirectUri, parameters);
    }

    @Override
    public boolean supports(final WebContext context) {
        return configurationContext.getOidcRequestSupport().isValidIssuerForEndpoint(context, OidcConstants.PUSHED_AUTHORIZE_URL)
               && context.getRequestURL().endsWith(OidcConstants.PUSHED_AUTHORIZE_URL)
               && context.getRequestParameter(OAuth20Constants.CLIENT_ID).isPresent();
    }

    @Override
    public boolean isSingleSignOnSessionRequired() {
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

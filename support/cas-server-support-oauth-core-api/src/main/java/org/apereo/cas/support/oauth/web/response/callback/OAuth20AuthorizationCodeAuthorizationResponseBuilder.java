package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.pac4j.core.context.WebContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashMap;

/**
 * This is {@link OAuth20AuthorizationCodeAuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class OAuth20AuthorizationCodeAuthorizationResponseBuilder extends BaseOAuth20AuthorizationResponseBuilder {
    /**
     * The Ticket registry.
     */
    protected final TicketRegistry ticketRegistry;

    private final OAuth20CodeFactory oAuthCodeFactory;

    public OAuth20AuthorizationCodeAuthorizationResponseBuilder(final ServicesManager servicesManager,
                                                                final CasConfigurationProperties casProperties,
                                                                final TicketRegistry ticketRegistry,
                                                                final OAuth20CodeFactory oAuthCodeFactory,
                                                                final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(servicesManager, casProperties, authorizationModelAndViewBuilder);
        this.ticketRegistry = ticketRegistry;
        this.oAuthCodeFactory = oAuthCodeFactory;
    }

    @Audit(action = AuditableActions.OAUTH2_CODE_RESPONSE,
        actionResolverName = AuditActionResolvers.OAUTH2_CODE_RESPONSE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OAUTH2_CODE_RESPONSE_RESOURCE_RESOLVER)
    @Override
    public ModelAndView build(final WebContext context, final String clientId,
                              final AccessTokenRequestDataHolder holder) {
        val authentication = holder.getAuthentication();
        val code = oAuthCodeFactory.create(holder.getService(), authentication,
            holder.getTicketGrantingTicket(), holder.getScopes(),
            holder.getCodeChallenge(), holder.getCodeChallengeMethod(),
            holder.getClientId(), holder.getClaims(),
            holder.getResponseType(), holder.getGrantType());
        LOGGER.debug("Generated OAuth code: [{}]", code);
        this.ticketRegistry.addTicket(code);
        return buildCallbackViewViaRedirectUri(context, clientId, authentication, code);
    }

    @Override
    public boolean supports(final WebContext context) {
        val responseType = OAuth20Utils.getRequestParameter(context, OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
        return StringUtils.equalsIgnoreCase(responseType, OAuth20ResponseTypes.CODE.getType());
    }

    /**
     * Build callback view via redirect uri model and view.
     *
     * @param context        the context
     * @param clientId       the client id
     * @param authentication the authentication
     * @param code           the code
     * @return the model and view
     */
    protected ModelAndView buildCallbackViewViaRedirectUri(final WebContext context, final String clientId,
                                                           final Authentication authentication,
                                                           final OAuth20Code code) {
        val attributes = authentication.getAttributes();
        val state = attributes.get(OAuth20Constants.STATE).get(0).toString();
        val nonce = attributes.get(OAuth20Constants.NONCE).get(0).toString();

        val redirectUri = OAuth20Utils.getRequestParameter(context, OAuth20Constants.REDIRECT_URI)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
        LOGGER.debug("Authorize request successful for client [{}] with redirect uri [{}]", clientId, redirectUri);

        val params = new LinkedHashMap<String, String>();
        params.put(OAuth20Constants.CODE, code.getId());
        if (StringUtils.isNotBlank(state)) {
            params.put(OAuth20Constants.STATE, state);
        }
        if (StringUtils.isNotBlank(nonce)) {
            params.put(OAuth20Constants.NONCE, nonce);
        }
        LOGGER.debug("Redirecting to URL [{}] with params [{}] for clientId [{}]", redirectUri, params.keySet(), clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        return build(context, registeredService, redirectUri, params);
    }
}

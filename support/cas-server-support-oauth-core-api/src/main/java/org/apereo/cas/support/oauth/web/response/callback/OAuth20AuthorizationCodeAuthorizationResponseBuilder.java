package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * This is {@link OAuth20AuthorizationCodeAuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class OAuth20AuthorizationCodeAuthorizationResponseBuilder extends BaseOAuth20AuthorizationResponseBuilder<OAuth20ConfigurationContext> {
    public OAuth20AuthorizationCodeAuthorizationResponseBuilder(
        final OAuth20ConfigurationContext context,
        final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(context, authorizationModelAndViewBuilder);
    }

    @Audit(action = AuditableActions.OAUTH2_CODE_RESPONSE,
        actionResolverName = AuditActionResolvers.OAUTH2_CODE_RESPONSE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OAUTH2_CODE_RESPONSE_RESOURCE_RESOLVER)
    @Override
    public ModelAndView build(final AccessTokenRequestContext holder) throws Exception {
        val authentication = holder.getAuthentication();
        val factory = (OAuth20CodeFactory) configurationContext.getTicketFactory().get(OAuth20Code.class);
        val code = factory.create(holder.getService(), authentication,
            holder.getTicketGrantingTicket(), holder.getScopes(),
            holder.getCodeChallenge(), holder.getCodeChallengeMethod(),
            holder.getClientId(), holder.getClaims(),
            holder.getResponseType(), holder.getGrantType());
        LOGGER.debug("Generated OAuth code: [{}]", code);
        configurationContext.getCentralAuthenticationService().addTicket(code);
        val ticketGrantingTicket = holder.getTicketGrantingTicket();
        Optional.ofNullable(ticketGrantingTicket).ifPresent(tgt -> {
            FunctionUtils.doAndHandle(ticket -> {
                configurationContext.getCentralAuthenticationService().updateTicket(ticket);
            }, (CheckedFunction<Throwable, TicketGrantingTicket>) throwable -> {
                LOGGER.error("Unable to update ticket-granting-ticket [{}]", ticketGrantingTicket, throwable);
                return null;
            }).accept(tgt);
        });
        return buildCallbackViewViaRedirectUri(holder, code);
    }

    @Override
    public boolean supports(final OAuth20AuthorizationRequest context) {
        return StringUtils.equalsIgnoreCase(context.getResponseType(), OAuth20ResponseTypes.CODE.getType());
    }

    /**
     * Build callback view via redirect uri model and view.
     *
     * @param code the code
     * @return the model and view
     * @throws Exception the exception
     */
    protected ModelAndView buildCallbackViewViaRedirectUri(final AccessTokenRequestContext holder,
                                                           final OAuth20Code code) throws Exception {
        val attributes = holder.getAuthentication().getAttributes();
        val state = attributes.get(OAuth20Constants.STATE).get(0).toString();
        val nonce = attributes.get(OAuth20Constants.NONCE).get(0).toString();

        LOGGER.debug("Authorize request successful for client [{}] with redirect uri [{}]", holder.getClientId(), holder.getRedirectUri());

        val params = new LinkedHashMap<String, String>();
        params.put(OAuth20Constants.CODE, code.getId());
        if (StringUtils.isNotBlank(state)) {
            params.put(OAuth20Constants.STATE, state);
        }
        if (StringUtils.isNotBlank(nonce)) {
            params.put(OAuth20Constants.NONCE, nonce);
        }
        LOGGER.debug("Redirecting to URL [{}] with params [{}] for clientId [{}]", holder.getRedirectUri(), params.keySet(), holder.getClientId());
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), holder.getClientId());
        return build(registeredService, holder.getResponseMode(), holder.getRedirectUri(), params);
    }
}

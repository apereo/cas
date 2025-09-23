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
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20CodeFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.springframework.web.servlet.ModelAndView;
import java.util.LinkedHashMap;
import java.util.Objects;
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

    @Audit(action = AuditableActions.OAUTH2_AUTHORIZATION_RESPONSE,
        actionResolverName = AuditActionResolvers.OAUTH2_AUTHORIZATION_RESPONSE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OAUTH2_AUTHORIZATION_RESPONSE_RESOURCE_RESOLVER)
    @Override
    public ModelAndView build(final AccessTokenRequestContext tokenRequestContext) throws Throwable {
        val authentication = tokenRequestContext.getAuthentication();
        val factory = (OAuth20CodeFactory) configurationContext.getTicketFactory().get(OAuth20Code.class);
        val code = factory.create(tokenRequestContext.getService(), authentication,
            tokenRequestContext.getTicketGrantingTicket(), tokenRequestContext.getScopes(),
            tokenRequestContext.getCodeChallenge(), tokenRequestContext.getCodeChallengeMethod(),
            tokenRequestContext.getClientId(), tokenRequestContext.getClaims(),
            tokenRequestContext.getResponseType(), tokenRequestContext.getGrantType());
        LOGGER.debug("Generated OAuth code: [{}]", code);
        val addedCode = configurationContext.getTicketRegistry().addTicket(code);
        Objects.requireNonNull(addedCode, () -> "Could not add OAuth code %s to the registry.".formatted(code.getId()));
        
        val ticketGrantingTicket = tokenRequestContext.getTicketGrantingTicket();
        Optional.ofNullable(ticketGrantingTicket).ifPresent(tgt -> FunctionUtils.doAndHandle(ticket -> {
            configurationContext.getTicketRegistry().updateTicket(ticket);
        }, (CheckedFunction<Throwable, Ticket>) throwable -> {
            LOGGER.error("Unable to update ticket-granting-ticket [{}]", ticketGrantingTicket, throwable);
            return null;
        }).accept(tgt));
        return buildCallbackViewViaRedirectUri(tokenRequestContext, addedCode);
    }

    @Override
    public boolean supports(final OAuth20AuthorizationRequest context) {
        return Strings.CI.equals(context.getResponseType(), OAuth20ResponseTypes.CODE.getType());
    }

    protected ModelAndView buildCallbackViewViaRedirectUri(final AccessTokenRequestContext holder,
                                                           final Ticket code) throws Exception {
        val attributes = holder.getAuthentication().getAttributes();
        LOGGER.debug("Authorize request successful for client [{}] with redirect uri [{}]", holder.getClientId(), holder.getRedirectUri());
        val params = new LinkedHashMap<String, String>();
        params.put(OAuth20Constants.CODE, code.getId());
        CollectionUtils.firstElement(attributes.get(OAuth20Constants.STATE)).ifPresent(state -> params.put(OAuth20Constants.STATE, state.toString()));
        CollectionUtils.firstElement(attributes.get(OAuth20Constants.NONCE)).ifPresent(nonce -> params.put(OAuth20Constants.NONCE, nonce.toString()));
        LOGGER.debug("Redirecting to URL [{}] with params [{}] for clientId [{}]", holder.getRedirectUri(), params.keySet(), holder.getClientId());
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), holder.getClientId());
        return build(registeredService, holder.getResponseMode(), holder.getRedirectUri(), params);
    }
}

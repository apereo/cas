package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

/**
 * This is {@link DelegatedAuthenticationSingleSignOnEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public record DelegatedAuthenticationSingleSignOnEvaluator(DelegatedClientAuthenticationConfigurationContext configurationContext) {
    /**
     * Single sign on session authorized for service boolean.
     *
     * @param requestContext the context
     * @return true /false
     */
    public boolean singleSignOnSessionAuthorizedForService(final RequestContext requestContext) {
        val resolvedService = resolveServiceFromRequestContext(requestContext);
        val authentication = getSingleSignOnAuthenticationFrom(requestContext);
        val authorized = authentication
            .map(authn -> configurationContext.getDelegatedClientIdentityProviderAuthorizers()
                .stream()
                .allMatch(Unchecked.predicate(authz -> authz.isDelegatedClientAuthorizedForAuthentication(authn, resolvedService, requestContext))))
            .orElse(Boolean.FALSE);
        val strategy = configurationContext.getSingleSignOnParticipationStrategy();
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .requestContext(requestContext)
            .build();
        return FunctionUtils.doUnchecked(() -> authorized && strategy.supports(ssoRequest) && strategy.isParticipating(ssoRequest));
    }

    /**
     * Resolve service from request context.
     *
     * @param context the context
     * @return the service
     */
    public Service resolveServiceFromRequestContext(final RequestContext context) {
        return FunctionUtils.doUnchecked(() -> {
            val service = WebUtils.getService(context);
            return configurationContext.getAuthenticationRequestServiceSelectionStrategies().resolveService(service);
        });
    }

    private Optional<Authentication> getSingleSignOnAuthenticationFrom(final RequestContext requestContext) {
        val tgtId = WebUtils.getTicketGrantingTicketId(requestContext);
        if (StringUtils.isBlank(tgtId)) {
            LOGGER.trace("No ticket-granting ticket could be located in the webflow context");
            return Optional.empty();
        }
        val ticket = configurationContext.getTicketRegistry().getTicket(tgtId, TicketGrantingTicket.class);
        LOGGER.trace("Located a valid ticket-granting ticket");
        return Optional.of(ticket.getAuthentication());
    }


    /**
     * Is there a current SSO session?
     *
     * @param requestContext the request context
     * @return whether there is a current SSO session
     */
    public boolean singleSignOnSessionExists(final RequestContext requestContext) {
        try {
            val authn = getSingleSignOnAuthenticationFrom(requestContext);
            if (authn.isPresent()) {
                LOGGER.trace("Located a valid ticket-granting ticket. Examining existing single sign-on session strategies...");
                val authentication = authn.get();
                val builder = configurationContext.getAuthenticationSystemSupport()
                    .establishAuthenticationContextFromInitial(authentication);
                LOGGER.trace("Recording and tracking initial authentication results in the request context");
                WebUtils.putAuthenticationResultBuilder(builder, requestContext);
                WebUtils.putAuthentication(authentication, requestContext);
                val strategy = configurationContext.getSingleSignOnParticipationStrategy();
                val ssoRequest = SingleSignOnParticipationRequest.builder()
                    .requestContext(requestContext)
                    .build();
                return FunctionUtils.doUnchecked(() -> strategy.supports(ssoRequest) && strategy.isParticipating(ssoRequest));
            }
        } catch (final AbstractTicketException e) {
            LOGGER.trace("Could not retrieve ticket id [{}] from registry.", e.getMessage());
        }
        LOGGER.trace("Ticket-granting ticket found in the webflow context is invalid or has expired");
        return false;
    }

}

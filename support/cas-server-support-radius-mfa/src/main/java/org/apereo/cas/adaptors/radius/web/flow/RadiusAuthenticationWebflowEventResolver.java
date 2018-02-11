package org.apereo.cas.adaptors.radius.web.flow;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.radius.AccessChallengedException;
import org.apereo.cas.adaptors.radius.authentication.RadiusTokenAuthenticationHandler;
import org.apereo.cas.adaptors.radius.authentication.RadiusTokenCredential;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link RadiusAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

@Slf4j
public class RadiusAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    public RadiusAuthenticationWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                    final CentralAuthenticationService centralAuthenticationService, 
                                                    final ServicesManager servicesManager,
                                                    final TicketRegistrySupport ticketRegistrySupport, 
                                                    final CookieGenerator warnCookieGenerator,
                                                    final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                    final MultifactorAuthenticationProviderSelector selector) {
        super(authenticationSystemSupport, centralAuthenticationService, 
                servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        try {
            final Credential credential = getCredentialFromContext(context);
            AuthenticationResultBuilder builder = WebUtils.getAuthenticationResultBuilder(context);

            LOGGER.debug("Handling authentication transaction for credential {}", credential);
            final Service service = WebUtils.getService(context);
            builder = this.authenticationSystemSupport.handleAuthenticationTransaction(service, builder, credential);

            LOGGER.debug("Issuing ticket-granting tickets for service {}", service);
            return ImmutableSet.of(grantTicketGrantingTicketToAuthenticationResult(context, builder, service));
        } catch (final AuthenticationException e) {
            final Class<? extends Throwable> error = e.getHandlerErrors().get(RadiusTokenAuthenticationHandler.class.getSimpleName()).getClass();
            final boolean accessChallenged = error != null && error == AccessChallengedException.class;
            if (accessChallenged) {
                // if access attempt was challenged, put message to webflow scope
                final Credential radiusCredential = getCredentialFromContext(context);
                final String message = radiusCredential instanceof RadiusTokenCredential ? ((RadiusTokenCredential) radiusCredential).getMessage() : "???:";
                context.getFlowScope().put("accessChallenged", message);
                return ImmutableSet.of(newEvent("accessChallenged"));
            } else {
                context.getFlowScope().put("accessChallenged", null);
                return ImmutableSet.of(newEvent("error", e));
            }
        } catch (final Exception e) {
            return ImmutableSet.of(newEvent("error", e));
        }
    }

    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}

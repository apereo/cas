package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.Set;

/**
 * This is {@link SurrogateWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogateWebflowEventResolver extends AbstractCasWebflowEventResolver {
    /**
     * Internal flag to indicate whether surrogate account selection is requested.
     */
    public static final String CONTEXT_ATTRIBUTE_REQUEST_SURROGATE = "requestSurrogateAccount";
    
    private final SurrogateAuthenticationService surrogateService;
    
    public SurrogateWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                         final CentralAuthenticationService centralAuthenticationService,
                                         final ServicesManager servicesManager,
                                         final TicketRegistrySupport ticketRegistrySupport,
                                         final CookieGenerator warnCookieGenerator,
                                         final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                         final MultifactorAuthenticationProviderSelector selector,
                                         final SurrogateAuthenticationService surrogateService) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport,
                warnCookieGenerator, authenticationSelectionStrategies, selector);
        this.surrogateService = surrogateService;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext requestContext) {
        if (requestContext.getFlowScope().getBoolean(CONTEXT_ATTRIBUTE_REQUEST_SURROGATE, Boolean.FALSE)) {
            requestContext.getFlowScope().remove(CONTEXT_ATTRIBUTE_REQUEST_SURROGATE);
            if (loadSurrogates(requestContext)) {
                return CollectionUtils.wrapSet(new Event(this, SurrogateWebflowConfigurer.VIEW_ID_SURROGATE_VIEW));
            }
        }
        return null;
    }

    private boolean loadSurrogates(final RequestContext requestContext) {
        final Credential c = WebUtils.getCredential(requestContext);
        if (c instanceof UsernamePasswordCredential) {
            final String username = c.getId();
            final Collection surrogates = surrogateService.getEligibleAccountsForSurrogateToProxy(username);
            if (!surrogates.isEmpty()) {
                surrogates.add(username);
                requestContext.getFlowScope().put("surrogates", surrogates);
                return true;
            }
        }
        return false;
    }
}

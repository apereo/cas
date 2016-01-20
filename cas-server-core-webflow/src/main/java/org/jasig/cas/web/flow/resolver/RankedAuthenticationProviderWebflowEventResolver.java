package org.jasig.cas.web.flow.resolver;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationResultBuilder;
import org.jasig.cas.authentication.AuthenticationSystemSupport;
import org.jasig.cas.authentication.DefaultAuthenticationSystemSupport;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.ticket.registry.TicketRegistrySupport;
import org.jasig.cas.web.flow.CasWebflowConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link RankedAuthenticationProviderWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("rankedAuthenticationProviderWebflowEventResolver")
public class RankedAuthenticationProviderWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Autowired
    @Qualifier("defaultAuthenticationSupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    /**
     * The Authentication system support.
     */
    @NotNull
    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final String tgt = WebUtils.getTicketGrantingTicketId(context);
        final RegisteredService service = WebUtils.getRegisteredService(context);

        if (service == null) {
            logger.debug("No service is available to determine event for principal");
            return resumeFlow();
        }

        if (StringUtils.isBlank(tgt)) {
            logger.trace("TGT is blank; proceed with flow normally.");
            return resumeFlow();
        }
        final Authentication authentication = ticketRegistrySupport.getAuthenticationFrom(tgt);
        if (authentication == null) {
            logger.trace("TGT has no authentication and is blank; proceed with flow normally.");
            return resumeFlow();
        }

        final AuthenticationResultBuilder builder = this.authenticationSystemSupport.handleInitialAuthenticationTransaction(authentication);
        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putAuthentication(authentication, context);

        final Event event = initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
        if (event == null) {
            logger.trace("Request does not indicate a requirement for authentication policy; proceed with flow normally.");
            return resumeFlow();
        }

        if (event.getId().equals(CasWebflowConstants.TRANSITION_ID_ERROR)) {
            return ImmutableSet.of(event);
        }

        final Set<MultifactorAuthenticationProvider> satisfiedProviders = getSatisfiedAuthenticationProviders(authentication);
        if (satisfiedProviders == null) {
            logger.debug("No satisfied multifactor authentication providers are recorded; proceed with flow normally.");
            return resumeFlow();
        }
        final Map<String, MultifactorAuthenticationProvider> providerMap = getAllMultifactorAuthenticationProvidersFromApplicationContext();

        final MultifactorAuthenticationProvider requestedProvider = locateRequestedProvider(providerMap.values(), event);
        if (requestedProvider == null) {
            logger.debug("Requested authentication provider is not available; proceed with flow normally.");
            return resumeFlow();
        }

        if (!satisfiedProviders.isEmpty()) {
            final MultifactorAuthenticationProvider[] providersArray = satisfiedProviders.toArray(new MultifactorAuthenticationProvider[]{});
            OrderComparator.sortIfNecessary(providersArray);
            for (final MultifactorAuthenticationProvider provider : providersArray) {
                if (provider.getOrder() > requestedProvider.getOrder()) {
                    logger.debug("Provider {} already satisfies the authentication requirements of {}; proceed with flow normally.",
                            provider, requestedProvider);
                    return resumeFlow();
                }
            }
        }
        return ImmutableSet.of(validateEventIdForMatchingTransitionInContext(requestedProvider.getId(), context,
                buildEventAttributeMap(authentication.getPrincipal(), service, requestedProvider)));
    }

    private Set<MultifactorAuthenticationProvider> getSatisfiedAuthenticationProviders(final Authentication authentication) {
        final Set<String> satisfiedHandlers = Set.class.cast(authentication.getAttributes()
                .get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
        if (satisfiedHandlers == null || satisfiedHandlers.isEmpty()) {
            return null;
        }
        final Set<MultifactorAuthenticationProvider> satisfiedProviders = new HashSet<>(satisfiedHandlers.size());
        for (final String satisfiedHandler : satisfiedHandlers) {
            final MultifactorAuthenticationProvider provider = getMultifactorAuthenticationProviderFromApplicationContext(satisfiedHandler);
            if (provider != null) {
                satisfiedProviders.add(provider);
            }
        }
        return satisfiedProviders;
    }

    private MultifactorAuthenticationProvider locateRequestedProvider(final Collection<MultifactorAuthenticationProvider> providersArray,
                                                                      final Event event) {
        for (final MultifactorAuthenticationProvider provider : providersArray) {
            if (provider.getId().equals(event.getId())) {
                return provider;
            }
        }
        return null;
    }

    private Set<Event> resumeFlow() {
        return ImmutableSet.of(new EventFactorySupport().success(this));
    }
}

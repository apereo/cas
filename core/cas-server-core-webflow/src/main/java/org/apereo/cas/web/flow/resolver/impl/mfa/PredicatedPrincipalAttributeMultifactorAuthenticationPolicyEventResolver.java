package org.apereo.cas.web.flow.resolver.impl.mfa;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.ResourceUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This is {@link PredicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class PredicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver
        extends PrincipalAttributeMultifactorAuthenticationPolicyEventResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(PredicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver.class);
    private static final Class[] PREDICATE_CTOR_PARAMETERS = {Object.class, Object.class, Object.class, Object.class};

    private final Resource predicateResource;

    public PredicatedPrincipalAttributeMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                                    final CentralAuthenticationService centralAuthenticationService,
                                                                                    final ServicesManager servicesManager,
                                                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                                                    final CookieGenerator warnCookieGenerator,
                                                                                    final AuthenticationServiceSelectionPlan authSelectionStrategies,
                                                                                    final MultifactorAuthenticationProviderSelector selector,
                                                                                    final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator, authSelectionStrategies, selector,
                casProperties);
        predicateResource = casProperties.getAuthn().getMfa().getGlobalPrincipalAttributePredicate();
    }

    @Override
    protected Set<Event> resolveMultifactorProviderViaPredicate(final RequestContext context,
                                                                final RegisteredService service,
                                                                final Principal principal,
                                                                final Collection<MultifactorAuthenticationProvider> providers) {
        try {
            if (predicateResource == null || !ResourceUtils.doesResourceExist(predicateResource)) {
                LOGGER.debug("No groovy script predicate is defined to decide which multifactor authentication provider should be chosen");
                return null;
            }

            final String script = IOUtils.toString(predicateResource.getInputStream(), StandardCharsets.UTF_8);
            final GroovyClassLoader classLoader = new GroovyClassLoader(getClass().getClassLoader(),
                    new CompilerConfiguration(), true);
            final Class<Predicate> predicateClass = classLoader.parseClass(script);
            final Object[] args = {service, principal, providers, LOGGER};

            LOGGER.debug("Preparing predicate arguments [{}]", args);
            final Constructor<Predicate> ctor = predicateClass.getDeclaredConstructor(PREDICATE_CTOR_PARAMETERS);
            final Predicate<MultifactorAuthenticationProvider> predicate = ctor.newInstance(args);

            LOGGER.debug("Created predicate instance [{}] from [{}] to filter multifactor authentication providers [{}]",
                    predicate.getClass().getSimpleName(), predicateResource, providers);

            if (providers == null || providers.isEmpty()) {
                LOGGER.error("No multifactor authentication providers are available in the application context");
                return null;
            }
            
            final MultifactorAuthenticationProvider provider = providers
                    .stream()
                    .filter(predicate)
                    .sorted(Comparator.comparingInt(MultifactorAuthenticationProvider::getOrder))
                    .findFirst()
                    .orElse(null);

            LOGGER.debug("Predicate instance [{}] returned multifactor authentication provider [{}]", predicate.getClass().getSimpleName(), provider);
            return evaluateEventForProviderInContext(principal, service, context, provider);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

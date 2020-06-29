package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * This is {@link MultifactorAuthenticationProviderResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationProviderResolver {
    Logger LOGGER = LoggerFactory.getLogger(MultifactorAuthenticationProviderResolver.class);


    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getName();
    }

    /**
     * Resolve event via authentication attribute set.
     *
     * @param authentication the authentication
     * @param attributeNames the attribute name
     * @param service        the service
     * @param context        the context
     * @param providers      the providers
     * @param predicate      the predicate
     * @return the set of resolved events
     */
    default Set<Event> resolveEventViaAuthenticationAttribute(final Authentication authentication,
                                                              final Collection<String> attributeNames,
                                                              final RegisteredService service,
                                                              final Optional<RequestContext> context,
                                                              final Collection<MultifactorAuthenticationProvider> providers,
                                                              final BiPredicate<String, MultifactorAuthenticationProvider> predicate) {
        return resolveEventViaAttribute(authentication.getPrincipal(), authentication.getAttributes(),
            attributeNames, service, context, providers, predicate);
    }

    /**
     * Resolve event via attribute set.
     *
     * @param principal           the principal
     * @param attributesToExamine the attributes to examine
     * @param attributeNames      the attribute names
     * @param service             the service
     * @param context             the context
     * @param providers           the providers
     * @param predicate           the predicate
     * @return the set
     */
    Set<Event> resolveEventViaAttribute(Principal principal,
                                        Map<String, List<Object>> attributesToExamine,
                                        Collection<String> attributeNames,
                                        RegisteredService service,
                                        Optional<RequestContext> context,
                                        Collection<MultifactorAuthenticationProvider> providers,
                                        BiPredicate<String, MultifactorAuthenticationProvider> predicate);

    /**
     * Resolve event via principal attribute set.
     *
     * @param principal      the principal
     * @param attributeNames the attribute name
     * @param service        the service
     * @param context        the context
     * @param providers      the providers
     * @param predicate      the predicate
     * @return the set of resolved events
     */
    default Set<Event> resolveEventViaPrincipalAttribute(final Principal principal,
                                                         final Collection<String> attributeNames,
                                                         final RegisteredService service,
                                                         final Optional<RequestContext> context,
                                                         final Collection<MultifactorAuthenticationProvider> providers,
                                                         final BiPredicate<String, MultifactorAuthenticationProvider> predicate) {
        if (attributeNames.isEmpty()) {
            LOGGER.trace("No attribute names are provided to trigger a multifactor authentication provider via [{}]", getName());
            return null;
        }

        if (providers == null || providers.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            return null;
        }

        val attributes = principal.getAttributes();
        return resolveEventViaAttribute(principal, attributes, attributeNames, service, context, providers, predicate);
    }

}

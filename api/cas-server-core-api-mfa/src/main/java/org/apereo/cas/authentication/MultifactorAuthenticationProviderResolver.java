package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.NamedObject;

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
public interface MultifactorAuthenticationProviderResolver extends NamedObject {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "multifactorAuthenticationProviderResolver";
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(MultifactorAuthenticationProviderResolver.class);
    

    /**
     * Resolve event via authentication attribute set.
     *
     * @param authentication    the authentication
     * @param attributeNames    the attribute name
     * @param registeredService the service
     * @param service           the service
     * @param context           the context
     * @param providers         the providers
     * @param predicate         the predicate
     * @return the set of resolved events
     */
    default Set<Event> resolveEventViaAuthenticationAttribute(final Authentication authentication,
                                                              final Collection<String> attributeNames,
                                                              final RegisteredService registeredService,
                                                              final Service service,
                                                              final Optional<RequestContext> context,
                                                              final Collection<MultifactorAuthenticationProvider> providers,
                                                              final BiPredicate<String, MultifactorAuthenticationProvider> predicate) {
        return resolveEventViaAttribute(authentication.getPrincipal(), authentication.getAttributes(),
            attributeNames, registeredService, service, context, providers, predicate);
    }

    /**
     * Resolve event via attribute set.
     *
     * @param principal           the principal
     * @param attributesToExamine the attributes to examine
     * @param attributeNames      the attribute names
     * @param registeredService   the service
     * @param service             the service
     * @param context             the context
     * @param providers           the providers
     * @param predicate           the predicate
     * @return the set
     */
    Set<Event> resolveEventViaAttribute(Principal principal,
                                        Map<String, List<Object>> attributesToExamine,
                                        Collection<String> attributeNames,
                                        RegisteredService registeredService,
                                        Service service,
                                        Optional<RequestContext> context,
                                        Collection<MultifactorAuthenticationProvider> providers,
                                        BiPredicate<String, MultifactorAuthenticationProvider> predicate);

    /**
     * Resolve event via principal attribute set.
     *
     * @param principal         the principal
     * @param attributeNames    the attribute name
     * @param registeredService the service
     * @param service           the service
     * @param context           the context
     * @param providers         the providers
     * @param predicate         the predicate
     * @return the set of resolved events
     */
    default Set<Event> resolveEventViaPrincipalAttribute(final Principal principal,
                                                         final Collection<String> attributeNames,
                                                         final RegisteredService registeredService,
                                                         final Service service,
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

        val resolvedPrincipal = resolvePrincipal(principal);
        LOGGER.debug("Multifactor authentication principal [{}] to evaluate for [{}] using attributes [{}]",
            resolvedPrincipal, providers, attributeNames);
        return resolveEventViaAttribute(principal, resolvedPrincipal.getAttributes(),
            attributeNames, registeredService, service, context, providers, predicate);
    }

    /**
     * Resolve principal.
     *
     * @param principal the principal
     * @return the principal
     */
    Principal resolvePrincipal(Principal principal);
}

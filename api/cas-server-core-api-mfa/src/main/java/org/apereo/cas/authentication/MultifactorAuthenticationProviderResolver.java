package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This is {@link MultifactorAuthenticationProviderResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
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
    Set<Event> resolveEventViaAuthenticationAttribute(Authentication authentication,
                                                      Collection<String> attributeNames,
                                                      RegisteredService service,
                                                      Optional<RequestContext> context,
                                                      Collection<MultifactorAuthenticationProvider> providers,
                                                      Predicate<String> predicate);

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
                                        Predicate<String> predicate);

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
    Set<Event> resolveEventViaPrincipalAttribute(Principal principal,
                                                 Collection<String> attributeNames,
                                                 RegisteredService service,
                                                 Optional<RequestContext> context,
                                                 Collection<MultifactorAuthenticationProvider> providers,
                                                 Predicate<String> predicate);

}

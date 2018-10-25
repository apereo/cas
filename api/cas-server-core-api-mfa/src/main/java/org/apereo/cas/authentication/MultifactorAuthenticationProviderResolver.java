package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.HashMap;
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


    /**
     * Build event attribute map map.
     *
     * @param principal the principal
     * @param service   the service
     * @param provider  the provider
     * @return the map
     */
    static Map<String, Object> buildEventAttributeMap(Principal principal,
                                                      Optional<RegisteredService> service,
                                                      MultifactorAuthenticationProvider provider) {
        val map = new HashMap<String, Object>();
        map.put(Principal.class.getName(), principal);
        service.ifPresent(svc -> map.put(RegisteredService.class.getName(), svc));
        map.put(MultifactorAuthenticationProvider.class.getName(), provider);
        return map;
    }

    default String getName() {
        return getClass().getName();
    }

    /**
     * Locate the provider in the collection, and have it match the requested mfa.
     * If the provider is multi-instance, resolve based on inner-registered providers.
     *
     * @param providers        the providers
     * @param requestMfaMethod the request mfa method
     * @return the optional
     */
    Optional<MultifactorAuthenticationProvider> resolveProvider(Map<String, MultifactorAuthenticationProvider> providers,
                                                                Collection<String> requestMfaMethod);

    /**
     * Locate the provider in the collection, and have it match the requested mfa.
     * If the provider is multi-instance, resolve based on inner-registered providers.
     *
     * @param providers        the providers
     * @param requestMfaMethod the request mfa method
     * @return the optional
     */
    Optional<MultifactorAuthenticationProvider> resolveProvider(Map<String, MultifactorAuthenticationProvider> providers,
                                                                String requestMfaMethod);

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

    Set<Event> resolveEventViaAttribute(Principal principal,
                                        Map<String, Object> attributesToExamine,
                                        Collection<String> attributeNames,
                                        RegisteredService service,
                                        Optional<RequestContext> context,
                                        Collection<MultifactorAuthenticationProvider> providers,
                                        Predicate<String> predicate);

    Event validateEventIdForMatchingTransitionInContext(String eventId,
                                                        Optional<RequestContext> context,
                                                        Map<String, Object> attributes);

    Set<Event> resolveEventViaMultivaluedAttribute(Principal principal,
                                                   Object attributeValue,
                                                   RegisteredService service,
                                                   Optional<RequestContext> context,
                                                   MultifactorAuthenticationProvider provider,
                                                   Predicate<String> predicate);

    Set<Event> resolveEventViaSingleAttribute(Principal principal,
                                              Object attributeValue,
                                              RegisteredService service,
                                              Optional<RequestContext> context,
                                              MultifactorAuthenticationProvider provider,
                                              Predicate<String> predicate);

    /**
     * Verify provider for current context and validate event id.
     *
     * @param principal the principal
     * @param service   the service
     * @param context   the context
     * @param provider  the provider
     * @return the set
     */
    Set<Event> evaluateEventForProviderInContext(Principal principal,
                                                 RegisteredService service,
                                                 Optional<RequestContext> context,
                                                 MultifactorAuthenticationProvider provider);

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

    /**
     * Gets principal attributes for multifactor authentication.
     *
     * @param principal the principal
     * @return the principal attributes for multifactor authentication
     */
    Map<String, Object> getPrincipalAttributesForMultifactorAuthentication(Principal principal);
}

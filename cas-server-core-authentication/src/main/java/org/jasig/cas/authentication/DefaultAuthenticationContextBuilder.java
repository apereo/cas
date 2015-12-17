package org.jasig.cas.authentication;

import com.google.common.collect.ImmutableSet;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link DefaultAuthenticationContextBuilder}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class DefaultAuthenticationContextBuilder implements AuthenticationContextBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationContextBuilder.class);
    private static final long serialVersionUID = 6180465589526463843L;

    private final Set<Authentication> authentications = Collections.synchronizedSet(new LinkedHashSet<Authentication>());

    private PrincipalElectionStrategy principalElectionStrategy;

    /**
     * Instantiates a new Default authentication context builder.
     *
     * @param principalElectionStrategy the principal election strategy
     */
    public DefaultAuthenticationContextBuilder(final PrincipalElectionStrategy principalElectionStrategy) {
        this.principalElectionStrategy = principalElectionStrategy;
    }

    @Override
    public AuthenticationContextBuilder collect(final Authentication authentication) {
        this.authentications.add(authentication);
        return this;
    }

    @Override
    public AuthenticationContext build() {
        return build(null);
    }

    @Override
    public AuthenticationContext build(final Service service) {
        final Authentication authentication = buildAuthentication();
        if (authentication == null) {
            LOGGER.info("Authentication context cannot be produced because no authentication is recorded into in the chain. Returning "
                    + "null");
            return null;
        }
        LOGGER.debug("Building an authentication context for authentication {} and service {}", authentication, service);
        return new DefaultAuthenticationContext(authentication, service);
    }

    private boolean isEmpty() {
        return this.authentications.isEmpty();
    }

    private Authentication buildAuthentication() {
        if (isEmpty()) {
            LOGGER.warn("No authentication event has been recorded; CAS cannot finalize the authentication context");
            return null;
        }
        final Map<String, Object> authenticationAttributes = new HashMap<>();
        final Map<String, Object> principalAttributes = new HashMap<>();
        final AuthenticationBuilder authenticationBuilder = DefaultAuthenticationBuilder.newInstance();

        buildAuthenticationHistory(this.authentications, authenticationAttributes, principalAttributes, authenticationBuilder);
        final Principal primaryPrincipal = getPrimaryPrincipal(this.authentications, principalAttributes);
        authenticationBuilder.setPrincipal(primaryPrincipal);
        LOGGER.debug("Determined primary authentication principal to be [{}]", primaryPrincipal);

        authenticationBuilder.setAttributes(authenticationAttributes);
        LOGGER.debug("Collected authentication attributes for this context are [{}]", authenticationAttributes);

        final DateTime dt = DateTime.now();
        authenticationBuilder.setAuthenticationDate(dt);
        LOGGER.debug("Authentication context commenced at [{}]", dt);

        return authenticationBuilder.build();

    }

    private void buildAuthenticationHistory(final Set<Authentication> authentications,
                                            final Map<String, Object> authenticationAttributes,
                                            final Map<String, Object> principalAttributes,
                                            final AuthenticationBuilder authenticationBuilder) {

        LOGGER.debug("Collecting authentication history based on [{}] authentication events", authentications.size());
        for (final Authentication authn : authentications) {
            final Principal authenticatedPrincipal = authn.getPrincipal();
            LOGGER.debug("Evaluating authentication principal [{}] for inclusion in context", authenticatedPrincipal);

            principalAttributes.putAll(authenticatedPrincipal.getAttributes());
            LOGGER.debug("Collected principal attributes [{}] for inclusion in context for principal [{}]",
                    principalAttributes, authenticatedPrincipal.getId());

            for (final String attrName : authn.getAttributes().keySet()) {
                if (!authenticationAttributes.containsKey(attrName)) {
                    final Object value = authn.getAttributes().get(attrName);
                    if (value != null) {
                        authenticationAttributes.put(attrName, value);
                        LOGGER.debug("Collected single authentication attribute [{}] -> [{}]", attrName, value);
                    } else {
                        LOGGER.warn("Authentication attribute [{}] has no value and is not collected", attrName);
                    }
                } else {
                    LOGGER.debug("Collecting multi-valued authentication attribute [{}]", attrName);
                    final Object oldValue = authenticationAttributes.remove(attrName);

                    LOGGER.debug("Converting authentication attribute [{}] to a collection of values", attrName);
                    final Collection<Object> listOfValues = convertValueToCollection(oldValue);
                    listOfValues.add(authn.getAttributes().get(attrName));
                    authenticationAttributes.put(attrName, listOfValues);
                    LOGGER.debug("Collected multi-valued authentication attribute [{}] -> [{}]", attrName, listOfValues);
                }
            }
            LOGGER.debug("Finalized authentication attributes [{}] for inclusion in authentication context",
                    authenticationAttributes);

            authenticationBuilder.addSuccesses(authn.getSuccesses())
                    .addFailures(authn.getFailures())
                    .addCredentials(authn.getCredentials());
        }
    }

    /**
     * Principal id is and must be enforced to be the same for all authentication contexts.
     * Based on that restriction, it's safe to simply grab the first principal id in the chain
     * when composing the authentication chain for the caller.
     */
    private Principal getPrimaryPrincipal(final Set<Authentication> authentications, final Map<String, Object> principalAttributes) {
        return this.principalElectionStrategy.nominate(ImmutableSet.copyOf(authentications), principalAttributes);
    }

    /**
     * Convert the object given into a {@link Collection} instead.
     * @param obj the object to convert into a collection
     * @return The collection instance containing the object provided
     */
    @SuppressWarnings("unchecked")
    public static Set<Object> convertValueToCollection(final Object obj) {
        final Set<Object> c = new HashSet<>();

        if (obj instanceof Collection) {
            c.addAll((Collection<Object>) obj);
            LOGGER.debug("Converting multi-valued attribute [{}] for the authentication context", obj);
        } else if (obj instanceof Map) {
            throw new UnsupportedOperationException(Map.class.getCanonicalName() + " is not supported");
        } else if (obj.getClass().isArray()) {
            c.addAll(Arrays.asList((Object[]) obj));
            LOGGER.debug("Converting array attribute [{}] for the authentication context", obj);
        } else {
            c.add(obj);
            LOGGER.debug("Converting attribute [{}] for the authentication context", obj);
        }
        return c;
    }

    public void setPrincipalElectionStrategy(final PrincipalElectionStrategy principalElectionStrategy) {
        this.principalElectionStrategy = principalElectionStrategy;
    }
}

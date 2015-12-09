package org.jasig.cas.authentication;

import com.google.common.collect.ImmutableSet;
import org.jasig.cas.authentication.principal.Principal;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
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
@Component("defaultAuthenticationContextBuilder")
public class DefaultAuthenticationContextBuilder implements AuthenticationContextBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationContext.class);

    private final Set<Authentication> authentications = new LinkedHashSet<>();

    @Autowired
    @Qualifier("principalElectionStrategy")
    private PrincipalElectionStrategy principalElectionStrategy;

    @Override
    public int count() {
        return this.authentications.size();
    }

    @Override
    public boolean isEmpty() {
        return this.authentications.isEmpty();
    }

    @Override
    public boolean collect(final Authentication authentication) throws AuthenticationException {
        if (this.authentications.add(authentication)) {
            LOGGER.debug("Collected authentication event. Associated principal with this authentication is [{}]",
                    authentication.getPrincipal());
            return true;
        }
        LOGGER.warn("Failed to collect authentication event. Associated principal with this authentication is [{}]",
                authentication.getPrincipal());
        return false;
    }

    @Override
    public AuthenticationContext build() {
        final Authentication authentication = buildAuthentication();
        if (authentication == null) {
            throw new RuntimeException(new GeneralSecurityException("Authentication context cannot be produced"));
        }
        return new DefaultAuthenticationContext(authentication);
    }

    @Override
    public void clear() {
        this.authentications.clear();
    }

    private Authentication buildAuthentication() {
        if (!isEmpty()) {
            final Map<String, Object> authenticationAttributes = new HashMap<>();
            final Map<String, Object> principalAttributes = new HashMap<>();
            final AuthenticationBuilder authenticationBuilder = DefaultAuthenticationBuilder.newInstance();

            buildAuthenticationHistory(authenticationAttributes, principalAttributes, authenticationBuilder);

            final Principal primaryPrincipal = getPrimaryPrincipal(principalAttributes);
            authenticationBuilder.setPrincipal(primaryPrincipal);
            LOGGER.debug("Determined primary authentication principal to be [{}]", primaryPrincipal);

            authenticationBuilder.setAttributes(authenticationAttributes);
            LOGGER.debug("Collected authentication attributes for this context are [{}]", authenticationAttributes);

            final DateTime dt = DateTime.now();
            authenticationBuilder.setAuthenticationDate(dt);
            LOGGER.debug("Authentication context commenced at [{}]", dt);

            return authenticationBuilder.build();
        }
        return null;
    }

    private void buildAuthenticationHistory(final Map<String, Object> authenticationAttributes,
                                            final Map<String, Object> principalAttributes,
                                            final AuthenticationBuilder authenticationBuilder) {

        LOGGER.debug("Collecting authentication history based on [{}] authentication events", this.authentications.size());
        for (final Authentication authn : this.authentications) {
            final Principal authenticatedPrincipal = authn.getPrincipal();
            LOGGER.debug("Evaluating authentication principal [{}] for inclusion in context", authenticatedPrincipal);

            principalAttributes.putAll(authenticatedPrincipal.getAttributes());
            LOGGER.debug("Collected principal attributes [{}] for inclusion in context for principal [{}]",
                    principalAttributes, authenticatedPrincipal.getId());

            for (final String attrName : authn.getAttributes().keySet()) {
                if (!authenticationAttributes.containsKey(attrName)) {
                    authenticationAttributes.put(attrName, authn.getAttributes().get(attrName));
                } else {
                    final Object oldValue = authenticationAttributes.remove(attrName);
                    final Collection<Object> listOfValues = convertValueToCollection(oldValue);

                    listOfValues.add(authn.getAttributes().get(attrName));
                    authenticationAttributes.put(attrName, listOfValues);
                }
            }
            LOGGER.debug("Collected authentication attributes [{}] for inclusion in context",
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
    private Principal getPrimaryPrincipal(final Map<String, Object> principalAttributes) {
        return this.principalElectionStrategy.nominate(ImmutableSet.copyOf(this.authentications), principalAttributes);
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

}

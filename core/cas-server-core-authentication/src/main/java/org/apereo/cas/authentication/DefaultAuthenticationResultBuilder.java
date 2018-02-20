package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link DefaultAuthenticationResultBuilder}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class DefaultAuthenticationResultBuilder implements AuthenticationResultBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationResultBuilder.class);
    private static final long serialVersionUID = 6180465589526463843L;

    private Credential providedCredential;

    private final Set<Authentication> authentications = Collections.synchronizedSet(new LinkedHashSet<>());

    private final PrincipalElectionStrategy principalElectionStrategy;

    /**
     * Instantiates a new default authentication result builder.
     *
     * @param principalElectionStrategy the principal election strategy
     */
    public DefaultAuthenticationResultBuilder(final PrincipalElectionStrategy principalElectionStrategy) {
        this.principalElectionStrategy = principalElectionStrategy;
    }

    @Override
    public Optional<Authentication> getInitialAuthentication() {
        if (this.authentications.isEmpty()) {
            LOGGER.warn("Authentication chain is empty as no authentications have been collected");
        }

        return this.authentications.stream().findFirst();
    }

    @Override
    public AuthenticationResultBuilder collect(final Authentication authentication) {
        if (authentication != null) {
            this.authentications.add(authentication);
        }
        return this;
    }

    @Override
    public AuthenticationResultBuilder collect(final Credential credential) {
        if (credential != null) {
            this.providedCredential = credential;
        }
        return this;
    }
    
    @Override
    public AuthenticationResult build() {
        return build(null);
    }

    @Override
    public AuthenticationResult build(final Service service) {
        final Authentication authentication = buildAuthentication();
        if (authentication == null) {
            LOGGER.info("Authentication result cannot be produced because no authentication is recorded into in the chain. Returning null");
            return null;
        }
        LOGGER.debug("Building an authentication result for authentication [{}] and service [{}]", authentication, service);
        final DefaultAuthenticationResult res = new DefaultAuthenticationResult(authentication, service);
        res.setCredentialProvided(this.providedCredential != null);
        return res;
    }

    private boolean isEmpty() {
        return this.authentications.isEmpty();
    }

    private Authentication buildAuthentication() {
        if (isEmpty()) {
            LOGGER.warn("No authentication event has been recorded; CAS cannot finalize the authentication result");
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
        LOGGER.debug("Collected authentication attributes for this result are [{}]", authenticationAttributes);

        authenticationBuilder.setAuthenticationDate(ZonedDateTime.now());
        final Authentication auth = authenticationBuilder.build();
        LOGGER.debug("Authentication result commenced at [{}]", auth.getAuthenticationDate());
        return auth;
    }

    private static void buildAuthenticationHistory(final Set<Authentication> authentications,
                                                   final Map<String, Object> authenticationAttributes,
                                                   final Map<String, Object> principalAttributes,
                                                   final AuthenticationBuilder authenticationBuilder) {

        LOGGER.debug("Collecting authentication history based on [{}] authentication events", authentications.size());
        authentications.stream().forEach(authn -> {
            final Principal authenticatedPrincipal = authn.getPrincipal();
            LOGGER.debug("Evaluating authentication principal [{}] for inclusion in result", authenticatedPrincipal);

            principalAttributes.putAll(authenticatedPrincipal.getAttributes());
            LOGGER.debug("Collected principal attributes [{}] for inclusion in this result for principal [{}]",
                    principalAttributes, authenticatedPrincipal.getId());

            authn.getAttributes().keySet().stream().forEach(attrName -> {
                if (authenticationAttributes.containsKey(attrName)) {
                    LOGGER.debug("Collecting multi-valued authentication attribute [{}]", attrName);
                    final Object oldValue = authenticationAttributes.remove(attrName);

                    LOGGER.debug("Converting authentication attribute [{}] to a collection of values", attrName);
                    final Collection<Object> listOfValues = CollectionUtils.toCollection(oldValue);
                    final Object newValue = authn.getAttributes().get(attrName);
                    listOfValues.addAll(CollectionUtils.toCollection(newValue));
                    authenticationAttributes.put(attrName, listOfValues);
                    LOGGER.debug("Collected multi-valued authentication attribute [{}] -> [{}]", attrName, listOfValues);
                } else {
                    final Object value = authn.getAttributes().get(attrName);
                    if (value != null) {
                        authenticationAttributes.put(attrName, value);
                        LOGGER.debug("Collected single authentication attribute [{}] -> [{}]", attrName, value);
                    } else {
                        LOGGER.warn("Authentication attribute [{}] has no value and is not collected", attrName);
                    }
                }
            });

            LOGGER.debug("Finalized authentication attributes [{}] for inclusion in this authentication result", authenticationAttributes);

            authenticationBuilder.addSuccesses(authn.getSuccesses())
                    .addFailures(authn.getFailures())
                    .addCredentials(authn.getCredentials());
        });
    }

    /**
     * Principal id is and must be enforced to be the same for all authentications.
     * Based on that restriction, it's safe to simply grab the first principal id in the chain
     * when composing the authentication chain for the caller.
     */
    private Principal getPrimaryPrincipal(final Set<Authentication> authentications, final Map<String, Object> principalAttributes) {
        return this.principalElectionStrategy.nominate(new HashSet<>(authentications), principalAttributes);
    }
}

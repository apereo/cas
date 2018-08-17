package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link DefaultAuthenticationResultBuilder}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@NoArgsConstructor
public class DefaultAuthenticationResultBuilder implements AuthenticationResultBuilder {

    private static final long serialVersionUID = 6180465589526463843L;
    private final Set<Authentication> authentications = Collections.synchronizedSet(new LinkedHashSet<>());
    private List<Credential> providedCredentials = new ArrayList<>();

    private static void buildAuthenticationHistory(final Set<Authentication> authentications,
                                                   final Map<String, Object> authenticationAttributes,
                                                   final Map<String, Object> principalAttributes,
                                                   final AuthenticationBuilder authenticationBuilder) {

        LOGGER.debug("Collecting authentication history based on [{}] authentication events", authentications.size());
        authentications.forEach(authn -> {
            val authenticatedPrincipal = authn.getPrincipal();
            LOGGER.debug("Evaluating authentication principal [{}] for inclusion in result", authenticatedPrincipal);

            principalAttributes.putAll(authenticatedPrincipal.getAttributes());
            LOGGER.debug("Collected principal attributes [{}] for inclusion in this result for principal [{}]",
                principalAttributes, authenticatedPrincipal.getId());

            authn.getAttributes().keySet().forEach(attrName -> {
                if (authenticationAttributes.containsKey(attrName)) {
                    LOGGER.debug("Collecting multi-valued authentication attribute [{}]", attrName);
                    val oldValue = authenticationAttributes.remove(attrName);

                    LOGGER.debug("Converting authentication attribute [{}] to a collection of values", attrName);
                    val listOfValues = CollectionUtils.toCollection(oldValue);
                    val newValue = authn.getAttributes().get(attrName);
                    listOfValues.addAll(CollectionUtils.toCollection(newValue));
                    authenticationAttributes.put(attrName, listOfValues);
                    LOGGER.debug("Collected multi-valued authentication attribute [{}] -> [{}]", attrName, listOfValues);
                } else {
                    val value = authn.getAttributes().get(attrName);
                    if (value != null) {
                        authenticationAttributes.put(attrName, value);
                        LOGGER.debug("Collected single authentication attribute [{}] -> [{}]", attrName, value);
                    } else {
                        LOGGER.warn("Authentication attribute [{}] has no value and is not collected", attrName);
                    }
                }
            });

            LOGGER.debug("Finalized authentication attributes [{}] for inclusion in this authentication result", authenticationAttributes);

            authenticationBuilder
                .addSuccesses(authn.getSuccesses())
                .addFailures(authn.getFailures())
                .addCredentials(authn.getCredentials());
        });
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
            this.providedCredentials.add(credential);
        }
        return this;
    }

    @Override
    public AuthenticationResult build(final PrincipalElectionStrategy principalElectionStrategy) {
        return build(principalElectionStrategy, null);
    }

    @Override
    public AuthenticationResult build(final PrincipalElectionStrategy principalElectionStrategy, final Service service) {
        val authentication = buildAuthentication(principalElectionStrategy);
        if (authentication == null) {
            LOGGER.info("Authentication result cannot be produced because no authentication is recorded into in the chain. Returning null");
            return null;
        }
        LOGGER.debug("Building an authentication result for authentication [{}] and service [{}]", authentication, service);
        val res = new DefaultAuthenticationResult(authentication, service);
        res.setCredentialProvided(!this.providedCredentials.isEmpty());
        return res;
    }

    private boolean isEmpty() {
        return this.authentications.isEmpty();
    }

    private Authentication buildAuthentication(final PrincipalElectionStrategy principalElectionStrategy) {
        if (isEmpty()) {
            LOGGER.warn("No authentication event has been recorded; CAS cannot finalize the authentication result");
            return null;
        }
        val authenticationAttributes = new HashMap<String, Object>();
        val principalAttributes = new HashMap<String, Object>();
        val authenticationBuilder = DefaultAuthenticationBuilder.newInstance();

        buildAuthenticationHistory(this.authentications, authenticationAttributes, principalAttributes, authenticationBuilder);
        val primaryPrincipal = getPrimaryPrincipal(principalElectionStrategy, this.authentications, principalAttributes);
        authenticationBuilder.setPrincipal(primaryPrincipal);
        LOGGER.debug("Determined primary authentication principal to be [{}]", primaryPrincipal);

        authenticationBuilder.setAttributes(authenticationAttributes);
        LOGGER.debug("Collected authentication attributes for this result are [{}]", authenticationAttributes);

        authenticationBuilder.setAuthenticationDate(ZonedDateTime.now());
        val auth = authenticationBuilder.build();
        LOGGER.debug("Authentication result commenced at [{}]", auth.getAuthenticationDate());
        return auth;
    }

    /**
     * Principal id is and must be enforced to be the same for all authentications.
     * Based on that restriction, it's safe to simply grab the first principal id in the chain
     * when composing the authentication chain for the caller.
     */
    private Principal getPrimaryPrincipal(final PrincipalElectionStrategy principalElectionStrategy,
                                          final Set<Authentication> authentications,
                                          final Map<String, Object> principalAttributes) {
        return principalElectionStrategy.nominate(new LinkedHashSet<>(authentications), principalAttributes);
    }
}

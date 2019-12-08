package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.ZoneOffset;
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
    private final Set<Authentication> authentications = Collections.synchronizedSet(new LinkedHashSet<>(0));
    private final List<Credential> providedCredentials = new ArrayList<>(0);

    private static void buildAuthenticationHistory(final Set<Authentication> authentications,
                                                   final Map<String, List<Object>> authenticationAttributes,
                                                   final Map<String, List<Object>> principalAttributes,
                                                   final AuthenticationBuilder authenticationBuilder) {

        LOGGER.trace("Collecting authentication history based on [{}] authentication events", authentications.size());
        authentications.forEach(authn -> {
            val authenticatedPrincipal = authn.getPrincipal();
            LOGGER.debug("Evaluating authentication principal [{}] for inclusion in result", authenticatedPrincipal);

            principalAttributes.putAll(CoreAuthenticationUtils.mergeAttributes(principalAttributes, authenticatedPrincipal.getAttributes()));
            LOGGER.debug("Collected principal attributes [{}] for inclusion in this result for principal [{}]",
                principalAttributes, authenticatedPrincipal.getId());

            authenticationAttributes.putAll(CoreAuthenticationUtils.mergeAttributes(authenticationAttributes, authn.getAttributes()));
            LOGGER.debug("Finalized authentication attributes [{}] for inclusion in this authentication result", authenticationAttributes);

            authenticationBuilder
                .addSuccesses(authn.getSuccesses())
                .addFailures(authn.getFailures())
                .addWarnings(authn.getWarnings())
                .addCredentials(authn.getCredentials());
        });
    }

    @Override
    public Optional<Authentication> getInitialAuthentication() {
        if (this.authentications.isEmpty()) {
            LOGGER.warn("Authentication chain is empty as no authentications have been collected");
        }

        synchronized (this.authentications) {
            return this.authentications.stream().findFirst();
        }
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
    public Optional<Credential> getInitialCredential() {
        if (this.providedCredentials.isEmpty()) {
            LOGGER.warn("Provided credentials chain is empty as no credentials have been collected");
        }
        return this.providedCredentials.stream().findFirst();
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
        LOGGER.trace("Building an authentication result for authentication [{}] and service [{}]", authentication, service);
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
        val authenticationAttributes = new HashMap<String, List<Object>>();
        val principalAttributes = new HashMap<String, List<Object>>();
        val authenticationBuilder = DefaultAuthenticationBuilder.newInstance();

        buildAuthenticationHistory(this.authentications, authenticationAttributes, principalAttributes, authenticationBuilder);

        synchronized (this.authentications) {
            val primaryPrincipal = getPrimaryPrincipal(principalElectionStrategy, this.authentications, principalAttributes);
            authenticationBuilder.setPrincipal(primaryPrincipal);
            LOGGER.debug("Determined primary authentication principal to be [{}]", primaryPrincipal);
        }

        authenticationBuilder.setAttributes(authenticationAttributes);
        LOGGER.trace("Collected authentication attributes for this result are [{}]", authenticationAttributes);

        authenticationBuilder.setAuthenticationDate(ZonedDateTime.now(ZoneOffset.UTC));

        val auth = authenticationBuilder.build();
        LOGGER.trace("Authentication result commenced at [{}]", auth.getAuthenticationDate());
        return auth;
    }

    /**
     * Principal id is and must be enforced to be the same for all authentications.
     * Based on that restriction, it's safe to simply grab the first principal id in the chain
     * when composing the authentication chain for the caller.
     */
    private static Principal getPrimaryPrincipal(final PrincipalElectionStrategy principalElectionStrategy,
                                                 final Set<Authentication> authentications,
                                                 final Map<String, List<Object>> principalAttributes) {
        return principalElectionStrategy.nominate(new LinkedHashSet<>(authentications), principalAttributes);
    }
}

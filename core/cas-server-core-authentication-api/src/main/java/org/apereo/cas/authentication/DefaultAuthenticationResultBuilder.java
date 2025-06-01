package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.merger.AttributeMerger;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serial;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
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
@Getter
@RequiredArgsConstructor
public class DefaultAuthenticationResultBuilder implements AuthenticationResultBuilder {

    @Serial
    private static final long serialVersionUID = 6180465589526463843L;

    private final Set<Authentication> authentications = Collections.synchronizedSet(new LinkedHashSet<>());

    private final List<Credential> providedCredentials = new ArrayList<>();

    private final PrincipalElectionStrategy principalElectionStrategy;

    /**
     * Principal id is and must be enforced to be the same for all authentications.
     * Based on that restriction, it's safe to grab the first principal id in the chain
     * when composing the authentication chain for the caller.
     */
    private static Principal getPrimaryPrincipal(final PrincipalElectionStrategy principalElectionStrategy,
                                                 final Set<Authentication> authentications,
                                                 final Map<String, List<Object>> principalAttributes) throws Throwable {
        return principalElectionStrategy.nominate(new LinkedHashSet<>(authentications), principalAttributes);
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
    public Optional<Credential> getInitialCredential() {
        if (this.providedCredentials.isEmpty()) {
            LOGGER.warn("Provided credentials chain is empty as no credentials have been collected");
        }
        return providedCredentials.stream().findFirst();
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationResultBuilder collect(final Authentication authentication) {
        Optional.ofNullable(authentication).ifPresent(authentications::add);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationResultBuilder collect(final Collection<Authentication> authentications) {
        this.authentications.addAll(authentications);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationResultBuilder collect(final Credential... credential) {
        providedCredentials.addAll(List.of(credential));
        return this;
    }
    
    @Override
    public AuthenticationResult build(final Service service) throws Throwable {
        val authentication = buildAuthentication(principalElectionStrategy);
        if (authentication == null) {
            LOGGER.info("Authentication result cannot be produced because no authentication is recorded into in the chain");
            return null;
        }
        LOGGER.trace("Building an authentication result for authentication [{}] and service [{}]", authentication, service);
        val authenticationResult = new DefaultAuthenticationResult(authentication, service);
        authenticationResult.setCredentialProvided(!this.providedCredentials.isEmpty());
        return authenticationResult;
    }

    protected void mergeAuthenticationAttributes(final Map<String, List<Object>> authenticationAttributes,
                                                 final AttributeMerger merger, final Authentication authn) {
        authenticationAttributes.putAll(CoreAuthenticationUtils.mergeAttributes(authenticationAttributes, authn.getAttributes(), merger));
        LOGGER.debug("Finalized authentication attributes [{}] for inclusion in this authentication result", authenticationAttributes);
    }

    protected void mergePrincipalAttributes(final Map<String, List<Object>> principalAttributes,
                                            final AttributeMerger merger,
                                            final Authentication authn) {
        val authenticatedPrincipal = authn.getPrincipal();
        LOGGER.debug("Evaluating authentication principal [{}] for inclusion in result", authenticatedPrincipal);

        principalAttributes.putAll(CoreAuthenticationUtils.mergeAttributes(principalAttributes, authenticatedPrincipal.getAttributes(), merger));
        LOGGER.debug("Collected principal attributes [{}] for inclusion in this result for principal [{}]",
            principalAttributes, authenticatedPrincipal.getId());
    }

    private void buildAuthenticationHistory(final Set<Authentication> authentications,
                                            final Map<String, List<Object>> authenticationAttributes,
                                            final Map<String, List<Object>> principalAttributes,
                                            final AuthenticationBuilder authenticationBuilder,
                                            final PrincipalElectionStrategy principalElectionStrategy) {

        val merger = principalElectionStrategy.getAttributeMerger();
        LOGGER.trace("Collecting authentication history based on [{}] authentication events", authentications.size());
        authentications.forEach(authn -> {
            mergePrincipalAttributes(principalAttributes, merger, authn);
            mergeAuthenticationAttributes(authenticationAttributes, merger, authn);

            authenticationBuilder
                .addSuccesses(authn.getSuccesses())
                .addFailures(authn.getFailures())
                .addWarnings(authn.getWarnings())
                .addCredentials(authn.getCredentials());
        });
    }

    private boolean isEmpty() {
        return this.authentications.isEmpty();
    }

    private Authentication buildAuthentication(final PrincipalElectionStrategy principalElectionStrategy) throws Throwable {
        if (isEmpty()) {
            LOGGER.warn("No authentication event has been recorded; CAS cannot finalize the authentication result");
            return null;
        }
        val authenticationAttributes = new HashMap<String, List<Object>>();
        val principalAttributes = new HashMap<String, List<Object>>();
        val authenticationBuilder = DefaultAuthenticationBuilder.newInstance();

        buildAuthenticationHistory(this.authentications, authenticationAttributes,
            principalAttributes, authenticationBuilder, principalElectionStrategy);

        synchronized (this.authentications) {
            val primaryPrincipal = getPrimaryPrincipal(principalElectionStrategy, this.authentications, principalAttributes);
            authenticationBuilder.setPrincipal(primaryPrincipal);
        }
        LOGGER.debug("Determined primary authentication principal to be [{}]", authenticationBuilder.getPrincipal());

        authenticationBuilder.setAttributes(authenticationAttributes);
        LOGGER.trace("Collected authentication attributes for this result are [{}]", authenticationAttributes);

        authenticationBuilder.setAuthenticationDate(ZonedDateTime.now(ZoneOffset.UTC));

        val auth = authenticationBuilder.build();
        LOGGER.trace("Authentication result commenced at [{}]", auth.getAuthenticationDate());
        return auth;
    }
}

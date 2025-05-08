package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link SurrogatePrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Setter
@Accessors(chain = true)
public class SurrogatePrincipalResolver extends PersonDirectoryPrincipalResolver {
    private SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder;

    public SurrogatePrincipalResolver(final PrincipalResolutionContext context) {
        super(context);
    }

    @Override
    public boolean supports(final Credential credential) {
        return super.supports(credential) && credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class)
            .stream()
            .anyMatch(trait -> StringUtils.isNotBlank(trait.getSurrogateUsername()));
    }

    @Override
    protected Principal buildResolvedPrincipal(final String id, final Map<String, List<Object>> attributes,
                                               final Credential credential, final Optional<Principal> currentPrincipal,
                                               final Optional<AuthenticationHandler> handler) throws Throwable {
        if (!supports(credential)) {
            return super.buildResolvedPrincipal(id, attributes, credential, currentPrincipal, handler);
        }
        val principal = currentPrincipal.orElseThrow(() -> new IllegalArgumentException("Current principal resolved cannot be empty"));
        return surrogatePrincipalBuilder.buildSurrogatePrincipal(credential, principal);
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Optional<Principal> currentPrincipal) {
        LOGGER.debug("Attempting to extract principal id for principal [{}]", currentPrincipal);
        if (!supports(credential)) {
            LOGGER.trace("Provided credential [{}] is not supported for surrogate authentication", credential);
            return super.extractPrincipalId(credential, currentPrincipal);
        }
        if (currentPrincipal.isEmpty()) {
            throw new IllegalArgumentException("Current principal resolved cannot be null");
        }
        val id = credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class)
            .map(SurrogateCredentialTrait::getSurrogateUsername)
            .orElseThrow();
        LOGGER.debug("Resolving principal id for surrogate authentication as [{}]", id);
        return id;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

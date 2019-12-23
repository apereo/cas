package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalElectionStrategy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.Ordered;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultPrincipalElectionStrategy} that selects the primary principal
 * to be the first principal in the chain of authentication history.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@RequiredArgsConstructor
@Setter
@Getter
public class DefaultPrincipalElectionStrategy implements PrincipalElectionStrategy {

    private static final long serialVersionUID = 6704726217030836315L;

    private final PrincipalFactory principalFactory;

    private int order = Ordered.LOWEST_PRECEDENCE;

    public DefaultPrincipalElectionStrategy() {
        this(PrincipalFactoryUtils.newPrincipalFactory());
    }

    @Override
    public Principal nominate(final Collection<Authentication> authentications,
                              final Map<String, List<Object>> principalAttributes) {
        val principal = getPrincipalFromAuthentication(authentications);
        val attributes = getPrincipalAttributesForPrincipal(principal, principalAttributes);
        val finalPrincipal = principalFactory.createPrincipal(principal.getId(), attributes);
        LOGGER.debug("Nominated [{}] as the primary principal", finalPrincipal);
        return finalPrincipal;
    }

    @Override
    public Principal nominate(final List<Principal> principals, final Map<String, List<Object>> attributes) {
        val principalIds = principals.stream()
            .filter(Objects::nonNull)
            .map(p -> p.getId().trim().toLowerCase())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        val count = principalIds.size();
        if (count > 1) {
            LOGGER.debug("Principal resolvers produced [{}] distinct principal [{}]; last resolved principal will be the principal", count, principalIds);
        }
        val principalId = principals.get(principals.size() - 1).getId();
        val finalPrincipal = this.principalFactory.createPrincipal(principalId, attributes);
        LOGGER.debug("Final principal constructed by the chain of resolvers is [{}]", finalPrincipal);
        return finalPrincipal;
    }

    /**
     * Gets principal attributes for principal.
     *
     * @param principal           the principal
     * @param principalAttributes the principal attributes
     * @return the principal attributes for principal
     */
    protected Map<String, List<Object>> getPrincipalAttributesForPrincipal(final Principal principal, final Map<String, List<Object>> principalAttributes) {
        return principalAttributes;
    }

    /**
     * Gets principal from authentication.
     *
     * @param authentications the authentications
     * @return the principal from authentication
     */
    protected Principal getPrincipalFromAuthentication(final Collection<Authentication> authentications) {
        return authentications.iterator().next().getPrincipal();
    }
}

package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.PrincipalElectionStrategyConflictResolver;
import org.apereo.cas.authentication.principal.merger.AttributeMerger;
import org.apereo.cas.authentication.principal.merger.ReplacingAttributeAdder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.Ordered;

import java.io.Serial;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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

    @Serial
    private static final long serialVersionUID = 6704726217030836315L;

    private final PrincipalFactory principalFactory;

    private final PrincipalElectionStrategyConflictResolver principalElectionConflictResolver;

    private AttributeMerger attributeMerger = new ReplacingAttributeAdder();

    private int order = Ordered.LOWEST_PRECEDENCE;

    public DefaultPrincipalElectionStrategy() {
        this(PrincipalFactoryUtils.newPrincipalFactory(), PrincipalElectionStrategyConflictResolver.last());
    }

    public DefaultPrincipalElectionStrategy(final PrincipalElectionStrategyConflictResolver principalElectionConflictResolver) {
        this(PrincipalFactoryUtils.newPrincipalFactory(), principalElectionConflictResolver);
    }

    @Override
    public Principal nominate(final Collection<Authentication> authentications,
                              final Map<String, List<Object>> principalAttributes) throws Throwable {
        val principal = getPrincipalFromAuthentication(authentications);
        val attributes = getPrincipalAttributesForPrincipal(authentications, principal, principalAttributes);
        val finalPrincipal = principalFactory.createPrincipal(principal.getId(), attributes);
        LOGGER.debug("Nominated [{}] as the primary principal", finalPrincipal);
        return finalPrincipal;
    }

    @Override
    public Principal nominate(final List<Principal> principals, final Map<String, List<Object>> attributes) throws Throwable {
        val principalIds = principals.stream()
            .filter(Objects::nonNull)
            .map(p -> p.getId().trim().toLowerCase(Locale.ENGLISH))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        val count = principalIds.size();
        if (count > 1) {
            LOGGER.debug("Principal resolvers produced [{}] distinct principals [{}]", count, principalIds);
        }
        return electPrincipal(principals, attributes);
    }

    protected Principal electPrincipal(final List<Principal> principals, final Map<String, List<Object>> attributes) throws Throwable {
        val principal = principalElectionConflictResolver.resolve(principals);
        val finalPrincipal = principalFactory.createPrincipal(principal.getId(), attributes);
        LOGGER.debug("Final principal constructed by the chain of resolvers is [{}]", finalPrincipal);
        return finalPrincipal;
    }

    protected Map<String, List<Object>> getPrincipalAttributesForPrincipal(final Collection<Authentication> authentications,
                                                                           final Principal principal,
                                                                           final Map<String, List<Object>> principalAttributes) {
        return principalAttributes;
    }

    protected Principal getPrincipalFromAuthentication(final Collection<Authentication> authentications) {
        val principals = authentications.stream().map(Authentication::getPrincipal).collect(Collectors.toList());
        return principalElectionConflictResolver.resolve(principals);
    }
}

package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalElectionStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link ChainingPrincipalElectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class ChainingPrincipalElectionStrategy implements PrincipalElectionStrategy {
    private static final long serialVersionUID = 3686895489996430873L;

    private final List<PrincipalElectionStrategy> chain;

    public ChainingPrincipalElectionStrategy(final PrincipalElectionStrategy... chain) {
        this.chain = Stream.of(chain).collect(Collectors.toList());
    }

    /**
     * Register election strategy.
     *
     * @param factory the factory
     */
    public void registerElectionStrategy(final PrincipalElectionStrategy factory) {
        this.chain.add(factory);
        AnnotationAwareOrderComparator.sort(this.chain);
    }

    @Override
    public Principal nominate(final Collection<Authentication> authentications,
                              final Map<String, List<Object>> principalAttributes) {
        val principal = this.chain
            .stream()
            .sorted(Comparator.comparing(PrincipalElectionStrategy::getOrder))
            .map(strategy -> strategy.nominate(authentications, principalAttributes))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow();
        LOGGER.trace("Nominated principal [{}] from authentication chain [{}]", principal, authentications);
        return principal;
    }

    @Override
    public Principal nominate(final List<Principal> principals, final Map<String, List<Object>> attributes) {
        val principal = this.chain
            .stream()
            .sorted(Comparator.comparing(PrincipalElectionStrategy::getOrder))
            .map(strategy -> strategy.nominate(principals, attributes))
            .findFirst()
            .orElseThrow();
        LOGGER.trace("Nominated principal [{}] from principal chain [{}]", principal, principals);
        return principal;
    }
}

package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.merger.AttributeMerger;
import org.apereo.cas.authentication.principal.merger.ReplacingAttributeAdder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * This is {@link ChainingPrincipalElectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
@Setter
public class ChainingPrincipalElectionStrategy implements PrincipalElectionStrategy {
    @Serial
    private static final long serialVersionUID = 3686895489996430873L;

    private final List<PrincipalElectionStrategy> chain;

    private AttributeMerger attributeMerger = new ReplacingAttributeAdder();

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
            .map(Unchecked.function(strategy -> strategy.nominate(authentications, principalAttributes)))
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
            .map(Unchecked.function(strategy -> strategy.nominate(principals, attributes)))
            .findFirst()
            .orElseThrow();
        LOGGER.trace("Nominated principal [{}] from principal chain [{}]", principal, principals);
        return principal;
    }
}

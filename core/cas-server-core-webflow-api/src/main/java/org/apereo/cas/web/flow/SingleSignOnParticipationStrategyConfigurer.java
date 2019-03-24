package org.apereo.cas.web.flow;

/**
 * This is {@link SingleSignOnParticipationStrategyConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
public interface SingleSignOnParticipationStrategyConfigurer {
    /**
     * Configure strategy.
     *
     * @param chain the chain
     */
    void configureStrategy(ChainingSingleSignOnParticipationStrategy chain);
}

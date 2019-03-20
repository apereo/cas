package org.apereo.cas.web.flow;

/**
 * This is {@link SingleSignOnParticipationStrategyConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
public interface SingleSignOnParticipationStrategyConfigurer {
    void configureStrategy(ChainingSingleSignOnParticipationStrategy chain);
}

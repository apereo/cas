package org.apereo.cas.config;

import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.ChainingPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.DefaultPrincipalResolutionExecutionPlan;
import org.apereo.cas.authentication.principal.PrincipalElectionStrategyConfigurer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolutionExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasCoreAuthenticationPrincipalConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreAuthenticationPrincipalConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreAuthenticationPrincipalConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "principalElectionStrategy")
    @Bean
    @RefreshScope
    @Autowired
    public PrincipalElectionStrategy principalElectionStrategy(final List<PrincipalElectionStrategyConfigurer> configurers) {
        LOGGER.trace("Building principal election strategies from [{}]", configurers);
        val chain = new ChainingPrincipalElectionStrategy();
        AnnotationAwareOrderComparator.sortIfNecessary(configurers);

        configurers.forEach(c -> {
            LOGGER.trace("Configuring principal selection strategy: [{}]", c);
            c.configurePrincipalElectionStrategy(chain);
        });
        return chain;
    }

    @ConditionalOnMissingBean(name = "defaultPrincipalElectionStrategyConfigurer")
    @Bean
    public PrincipalElectionStrategyConfigurer defaultPrincipalElectionStrategyConfigurer() {
        return chain -> chain.registerElectionStrategy(new DefaultPrincipalElectionStrategy(principalFactory()));
    }

    @ConditionalOnMissingBean(name = "principalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory principalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "globalPrincipalAttributeRepository")
    public RegisteredServicePrincipalAttributesRepository globalPrincipalAttributeRepository() {
        val props = casProperties.getAuthn().getAttributeRepository();
        val cacheTime = props.getExpirationTime();
        if (cacheTime <= 0) {
            LOGGER.warn("Caching for the global principal attribute repository is disabled");
            return new DefaultPrincipalAttributesRepository();
        }
        return new CachingPrincipalAttributesRepository(props.getExpirationTimeUnit().toUpperCase(), cacheTime);
    }


    @Bean
    @ConditionalOnMissingBean(name = "defaultPrincipalResolver")
    @RefreshScope
    @Autowired
    public PrincipalResolver defaultPrincipalResolver(final List<PrincipalResolutionExecutionPlanConfigurer> configurers,
                                                      @Qualifier("principalElectionStrategy") final PrincipalElectionStrategy principalElectionStrategy) {
        val plan = new DefaultPrincipalResolutionExecutionPlan();
        val sortedConfigurers = new ArrayList<PrincipalResolutionExecutionPlanConfigurer>(configurers);
        AnnotationAwareOrderComparator.sortIfNecessary(sortedConfigurers);

        sortedConfigurers.forEach(c -> {
            LOGGER.trace("Configuring principal resolution execution plan [{}]", c.getName());
            c.configurePrincipalResolutionExecutionPlan(plan);
        });
        plan.registerPrincipalResolver(new EchoingPrincipalResolver());

        val registeredPrincipalResolvers = plan.getRegisteredPrincipalResolvers();
        val resolver = new ChainingPrincipalResolver(principalElectionStrategy);
        resolver.setChain(registeredPrincipalResolvers);
        return resolver;
    }
}

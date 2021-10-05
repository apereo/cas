package org.apereo.cas.config;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
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
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link CasCoreAuthenticationPrincipalConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casCoreAuthenticationPrincipalConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreAuthenticationPrincipalConfiguration {

    @Configuration(value = "CasCoreAuthenticationPrincipalResolutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationPrincipalResolutionConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "defaultPrincipalResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public PrincipalResolver defaultPrincipalResolver(
            final ObjectProvider<List<PrincipalResolutionExecutionPlanConfigurer>> configurers,
            final CasConfigurationProperties casProperties,
            @Qualifier("principalElectionStrategy")
            final PrincipalElectionStrategy principalElectionStrategy) {
            val plan = new DefaultPrincipalResolutionExecutionPlan();
            val sortedConfigurers = new ArrayList<>(Optional.ofNullable(configurers.getIfAvailable()).orElse(new ArrayList<>(0)));
            AnnotationAwareOrderComparator.sortIfNecessary(sortedConfigurers);

            sortedConfigurers.forEach(Unchecked.consumer(c -> {
                LOGGER.trace("Configuring principal resolution execution plan [{}]", c.getName());
                c.configurePrincipalResolutionExecutionPlan(plan);
            }));
            plan.registerPrincipalResolver(new EchoingPrincipalResolver());

            val registeredPrincipalResolvers = plan.getRegisteredPrincipalResolvers();
            val resolver = new ChainingPrincipalResolver(principalElectionStrategy, casProperties);
            resolver.setChain(registeredPrincipalResolvers);
            return resolver;
        }
    }

    @Configuration(value = "CasCoreAuthenticationPrincipalElectionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationPrincipalElectionConfiguration {
        @ConditionalOnMissingBean(name = "principalElectionStrategy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public PrincipalElectionStrategy principalElectionStrategy(final List<PrincipalElectionStrategyConfigurer> configurers,
                                                                   final CasConfigurationProperties casProperties) {
            LOGGER.trace("Building principal election strategies from [{}]", configurers);
            val chain = new ChainingPrincipalElectionStrategy();
            val merger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
            chain.setAttributeMerger(merger);
            AnnotationAwareOrderComparator.sortIfNecessary(configurers);

            configurers.forEach(c -> {
                LOGGER.trace("Configuring principal selection strategy: [{}]", c);
                c.configurePrincipalElectionStrategy(chain);
            });
            return chain;
        }

        @ConditionalOnMissingBean(name = "defaultPrincipalElectionStrategyConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public PrincipalElectionStrategyConfigurer defaultPrincipalElectionStrategyConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier("principalFactory")
            final PrincipalFactory principalFactory) {
            return chain -> {
                val strategy = new DefaultPrincipalElectionStrategy(principalFactory,
                    CoreAuthenticationUtils.newPrincipalElectionStrategyConflictResolver(casProperties.getPersonDirectory()));
                val merger = CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger());
                strategy.setAttributeMerger(merger);
                chain.registerElectionStrategy(strategy);
            };
        }

    }

    @Configuration(value = "CasCoreAuthenticationPrincipalFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreAuthenticationPrincipalFactoryConfiguration {

        @ConditionalOnMissingBean(name = "principalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory principalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = PrincipalResolver.BEAN_NAME_GLOBAL_PRINCIPAL_ATTRIBUTE_REPOSITORY)
        @Autowired
        public RegisteredServicePrincipalAttributesRepository globalPrincipalAttributeRepository(final CasConfigurationProperties casProperties) {
            val props = casProperties.getAuthn().getAttributeRepository().getCore();
            val cacheTime = props.getExpirationTime();
            if (cacheTime <= 0) {
                LOGGER.warn("Caching for the global principal attribute repository is disabled");
                return new DefaultPrincipalAttributesRepository();
            }
            return new CachingPrincipalAttributesRepository(props.getExpirationTimeUnit().toUpperCase(), cacheTime);
        }

    }


}

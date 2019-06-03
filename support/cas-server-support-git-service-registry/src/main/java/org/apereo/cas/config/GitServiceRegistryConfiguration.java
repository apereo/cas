package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.git.GitRepositoryBuilder;
import org.apereo.cas.services.GitServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * This is {@link GitServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("gitServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class GitServiceRegistryConfiguration {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("registeredServiceResourceNamingStrategy")
    private ObjectProvider<RegisteredServiceResourceNamingStrategy> resourceNamingStrategy;

    @Autowired
    @Qualifier("serviceRegistryListeners")
    private ObjectProvider<Collection<ServiceRegistryListener>> serviceRegistryListeners;

    @Bean
    @ConditionalOnMissingBean(name = "gitRepositoryInstance")
    public GitRepository gitRepositoryInstance() {
        val registry = casProperties.getServiceRegistry().getGit();
        return new GitRepositoryBuilder(registry.getRepositoryUrl())
            .activeBranch(registry.getActiveBranch())
            .branchesToClone(registry.getBranchesToClone())
            .credentialProvider(registry.getUsername(), registry.getPassword())
            .repositoryDirectory(registry.getCloneDirectory())
            .build();
    }

    @Bean
    public ServiceRegistry gitServiceRegistry() {
        val registry = casProperties.getServiceRegistry().getGit();
        return new GitServiceRegistry(eventPublisher,
            gitRepositoryInstance(),
            CollectionUtils.wrapList(
                new RegisteredServiceJsonSerializer(),
                new RegisteredServiceYamlSerializer()
            ),
            resourceNamingStrategy.getIfAvailable(),
            registry.isPushChanges(),
            serviceRegistryListeners.getIfAvailable()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "gitServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer gitServiceRegistryExecutionPlanConfigurer() {
        return new ServiceRegistryExecutionPlanConfigurer() {
            @Override
            public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
                plan.registerServiceRegistry(gitServiceRegistry());
            }
        };
    }
}

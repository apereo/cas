package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.git.GitRepositoryBuilder;
import org.apereo.cas.services.GitServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.services.locator.DefaultGitRepositoryRegisteredServiceLocator;
import org.apereo.cas.services.locator.GitRepositoryRegisteredServiceLocator;
import org.apereo.cas.services.locator.TypeAwareGitRepositoryRegisteredServiceLocator;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link CasGitServiceRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "git")
@AutoConfiguration
public class CasGitServiceRegistryAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.service-registry.git.repository-url");

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "gitServiceRegistryRepositoryInstance")
    public GitRepository gitServiceRegistryRepositoryInstance(
        final CasConfigurationProperties casProperties) {
        val registry = casProperties.getServiceRegistry().getGit();
        return GitRepositoryBuilder.newInstance(registry).build();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "gitServiceRegistry")
    public ServiceRegistry gitServiceRegistry(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<@NonNull List<ServiceRegistryListener>> serviceRegistryListeners,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("gitServiceRegistryRepositoryInstance")
        final GitRepository gitServiceRegistryRepositoryInstance,
        @Qualifier(RegisteredServiceResourceNamingStrategy.BEAN_NAME)
        final RegisteredServiceResourceNamingStrategy resourceNamingStrategy) {

        return BeanSupplier.of(ServiceRegistry.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val properties = casProperties.getServiceRegistry().getGit();
                val locators = new ArrayList<GitRepositoryRegisteredServiceLocator>();
                if (properties.isGroupByType()) {
                    locators.add(new TypeAwareGitRepositoryRegisteredServiceLocator(resourceNamingStrategy,
                        gitServiceRegistryRepositoryInstance.getRepositoryDirectory(), properties));
                }
                locators.add(new DefaultGitRepositoryRegisteredServiceLocator(resourceNamingStrategy,
                    gitServiceRegistryRepositoryInstance.getRepositoryDirectory(), properties));
                return new GitServiceRegistry(applicationContext, gitServiceRegistryRepositoryInstance,
                    CollectionUtils.wrapList(new RegisteredServiceJsonSerializer(applicationContext),
                        new RegisteredServiceYamlSerializer(applicationContext)),
                    properties.isPushChanges(), properties.getRootDirectory(),
                    Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new), locators);
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "gitServiceRegistryExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public ServiceRegistryExecutionPlanConfigurer gitServiceRegistryExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("gitServiceRegistry")
        final ServiceRegistry gitServiceRegistry) {
        return BeanSupplier.of(ServiceRegistryExecutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerServiceRegistry(gitServiceRegistry))
            .otherwiseProxy()
            .get();
    }
}

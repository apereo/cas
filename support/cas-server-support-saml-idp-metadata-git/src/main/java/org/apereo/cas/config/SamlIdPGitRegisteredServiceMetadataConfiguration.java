package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.git.GitRepositoryBuilder;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.GitSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This is {@link SamlIdPGitRegisteredServiceMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLServiceProviderMetadata, module = "git")
@Configuration(value = "SamlIdPGitRegisteredServiceMetadataConfiguration", proxyBeanMethods = false)
class SamlIdPGitRegisteredServiceMetadataConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.saml-idp.metadata.git.repository-url");

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "gitSamlRegisteredServiceRepositoryInstance")
    public GitRepository gitSamlRegisteredServiceRepositoryInstance(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(GitRepository.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val git = casProperties.getAuthn().getSamlIdp().getMetadata().getGit();
                return GitRepositoryBuilder.newInstance(git).build();
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlRegisteredServiceMetadataResolver gitSamlRegisteredServiceMetadataResolver(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("gitSamlRegisteredServiceRepositoryInstance")
        final GitRepository gitSamlRegisteredServiceRepositoryInstance,
        @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
        final OpenSamlConfigBean openSamlConfigBean) {
        return BeanSupplier.of(SamlRegisteredServiceMetadataResolver.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val idp = casProperties.getAuthn().getSamlIdp();
                return new GitSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean,
                    gitSamlRegisteredServiceRepositoryInstance);
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "gitSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer gitSamlRegisteredServiceMetadataResolutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("gitSamlRegisteredServiceMetadataResolver")
        final SamlRegisteredServiceMetadataResolver gitSamlRegisteredServiceMetadataResolver) {
        return BeanSupplier.of(SamlRegisteredServiceMetadataResolutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerMetadataResolver(gitSamlRegisteredServiceMetadataResolver))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "gitSamlRegisteredServiceRepositoryScheduler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Lazy(false)
    public Runnable gitSamlRegisteredServiceRepositoryScheduler(
        @Qualifier("gitSamlRegisteredServiceRepositoryInstance")
        final GitRepository gitSamlRegisteredServiceRepositoryInstance,
        final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(Runnable.class)
            .when(BeanCondition.on("cas.authn.saml-idp.metadata.git.schedule.enabled")
                .isTrue().given(applicationContext.getEnvironment()))
            .supply(() -> new GitSamlRegisteredServiceRepositoryScheduler(gitSamlRegisteredServiceRepositoryInstance))
            .otherwiseProxy()
            .get();
    }

    @RequiredArgsConstructor
    @Slf4j
    static class GitSamlRegisteredServiceRepositoryScheduler implements Runnable {
        private final GitRepository gitRepository;

        @Scheduled(
            cron = "${cas.authn.saml-idp.metadata.git.schedule.cron-expression:}",
            zone = "${cas.authn.saml-idp.metadata.git.schedule.cron-time-zone:}",
            initialDelayString = "${cas.authn.saml-idp.metadata.git.schedule.start-delay:PT60S}",
            fixedDelayString = "${cas.authn.saml-idp.metadata.git.schedule.repeat-interval:PT2H}")
        @Override
        public void run() {
            FunctionUtils.doUnchecked(_ -> {
                val origin = StringUtils.defaultIfBlank(gitRepository.getRepositoryRemote("origin"), "default");
                LOGGER.debug("Starting to pull SAML registered services from [{}]", origin);
                gitRepository.pull();
            });
        }
    }
}

package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.git.GitRepositoryBuilder;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.GitSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SamlIdPGitRegisteredServiceMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.SAMLServiceProviderMetadata, module = "git")
@AutoConfiguration
public class SamlIdPGitRegisteredServiceMetadataConfiguration {
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
}

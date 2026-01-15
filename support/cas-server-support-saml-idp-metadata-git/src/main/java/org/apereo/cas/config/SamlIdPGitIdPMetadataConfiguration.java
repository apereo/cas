package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.git.GitRepositoryBuilder;
import org.apereo.cas.support.saml.idp.metadata.GitSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.GitSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.GitSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SamlIdPGitIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProviderMetadata, module = "git")
@Configuration(value = "SamlIdPGitIdPMetadataConfiguration", proxyBeanMethods = false)
class SamlIdPGitIdPMetadataConfiguration {
    private static final BeanCondition CONDITION_ENABLED = BeanCondition.on("cas.authn.saml-idp.metadata.git.idp-metadata-enabled").isTrue();

    private static final BeanCondition CONDITION_URL = BeanCondition.on("cas.authn.saml-idp.metadata.git.repository-url");

    @Bean
    @ConditionalOnMissingBean(name = "gitSamlIdPMetadataCipherExecutor")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(CipherExecutor.class)
            .when(CONDITION_ENABLED.given(applicationContext.getEnvironment()))
            .and(CONDITION_URL.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val idp = casProperties.getAuthn().getSamlIdp();
                val crypto = idp.getMetadata().getGit().getCrypto();
                if (crypto.isEnabled()) {
                    return CipherExecutorUtils.newStringCipherExecutor(crypto, GitSamlIdPMetadataCipherExecutor.class);
                }
                LOGGER.info("Git SAML IdP metadata encryption/signing is turned off and MAY NOT be safe in a production environment. "
                            + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
                return CipherExecutor.noOp();
            })
            .otherwise(CipherExecutor::noOp)
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "gitIdPMetadataRepositoryInstance")
    public GitRepository gitIdPMetadataRepositoryInstance(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(GitRepository.class)
            .when(CONDITION_ENABLED.given(applicationContext.getEnvironment()))
            .and(CONDITION_URL.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val git = casProperties.getAuthn().getSamlIdp().getMetadata().getGit();
                return GitRepositoryBuilder.newInstance(git).build();
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("gitIdPMetadataRepositoryInstance")
        final GitRepository gitIdPMetadataRepositoryInstance,
        @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
        final SamlIdPMetadataGeneratorConfigurationContext ctx) {
        return BeanSupplier.of(SamlIdPMetadataGenerator.class)
            .when(CONDITION_ENABLED.given(applicationContext.getEnvironment()))
            .and(CONDITION_URL.given(applicationContext.getEnvironment()))
            .supply(() -> new GitSamlIdPMetadataGenerator(ctx, gitIdPMetadataRepositoryInstance))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataLocator samlIdPMetadataLocator(
        @Qualifier("samlIdPMetadataGeneratorCipherExecutor")
        final CipherExecutor samlIdPMetadataGeneratorCipherExecutor,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("samlIdPMetadataCache")
        final Cache<@NonNull String, SamlIdPMetadataDocument> samlIdPMetadataCache,
        @Qualifier("gitIdPMetadataRepositoryInstance")
        final GitRepository gitIdPMetadataRepositoryInstance) {
        return BeanSupplier.of(SamlIdPMetadataLocator.class)
            .when(CONDITION_ENABLED.given(applicationContext.getEnvironment()))
            .and(CONDITION_URL.given(applicationContext.getEnvironment()))
            .supply(() -> new GitSamlIdPMetadataLocator(gitIdPMetadataRepositoryInstance,
                samlIdPMetadataCache, samlIdPMetadataGeneratorCipherExecutor, applicationContext))
            .otherwiseProxy()
            .get();
    }
}

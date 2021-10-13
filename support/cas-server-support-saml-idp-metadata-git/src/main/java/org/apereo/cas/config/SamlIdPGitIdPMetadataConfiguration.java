package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
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

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.git", name = {"idp-metadata-enabled", "repository-url"})
@Configuration(value = "samlIdPGitIdPMetadataConfiguration", proxyBeanMethods = false)
public class SamlIdPGitIdPMetadataConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "gitSamlIdPMetadataCipherExecutor")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public CipherExecutor samlIdPMetadataGeneratorCipherExecutor(final CasConfigurationProperties casProperties) {
        val idp = casProperties.getAuthn().getSamlIdp();
        val crypto = idp.getMetadata().getGit().getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, GitSamlIdPMetadataCipherExecutor.class);
        }
        LOGGER.info("Git SAML IdP metadata encryption/signing is turned off and MAY NOT be safe in a production environment. "
            + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
        return CipherExecutor.noOp();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "gitIdPMetadataRepositoryInstance")
    @Autowired
    public GitRepository gitIdPMetadataRepositoryInstance(final CasConfigurationProperties casProperties) {
        val git = casProperties.getAuthn().getSamlIdp().getMetadata().getGit();
        return GitRepositoryBuilder.newInstance(git).build();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator(
        @Qualifier("gitIdPMetadataRepositoryInstance")
        final GitRepository gitIdPMetadataRepositoryInstance,
        @Qualifier("samlIdPMetadataGeneratorConfigurationContext")
        final SamlIdPMetadataGeneratorConfigurationContext samlIdPMetadataGeneratorConfigurationContext) {
        return new GitSamlIdPMetadataGenerator(samlIdPMetadataGeneratorConfigurationContext, gitIdPMetadataRepositoryInstance);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdPMetadataLocator samlIdPMetadataLocator(
        @Qualifier("samlIdPMetadataCache")
        final Cache<String, SamlIdPMetadataDocument> samlIdPMetadataCache,
        @Qualifier("gitIdPMetadataRepositoryInstance")
        final GitRepository gitIdPMetadataRepositoryInstance) {
        return new GitSamlIdPMetadataLocator(gitIdPMetadataRepositoryInstance, samlIdPMetadataCache);
    }
}

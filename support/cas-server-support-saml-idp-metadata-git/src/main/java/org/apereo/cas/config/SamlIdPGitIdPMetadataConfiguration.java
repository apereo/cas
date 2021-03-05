package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.git.GitRepositoryBuilder;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.idp.metadata.GitSamlIdPMetadataCipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.GitSamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.GitSamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGeneratorConfigurationContext;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdPGitIdPMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("samlIdPGitIdPMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.git", name = { "idp-metadata-enabled", "repository-url" })
public class SamlIdPGitIdPMetadataConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;
    
    @Autowired
    @Qualifier("samlIdPMetadataCache")
    private ObjectProvider<Cache<String, SamlIdPMetadataDocument>> samlIdPMetadataCache;

    @Autowired
    @Qualifier("samlSelfSignedCertificateWriter")
    private ObjectProvider<SamlIdPCertificateAndKeyWriter> samlSelfSignedCertificateWriter;

    @Bean
    @ConditionalOnMissingBean(name = "gitSamlIdPMetadataCipherExecutor")
    @RefreshScope
    public CipherExecutor gitSamlIdPMetadataCipherExecutor() {
        val idp = casProperties.getAuthn().getSamlIdp();
        val crypto = idp.getMetadata().getGit().getCrypto();

        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, GitSamlIdPMetadataCipherExecutor.class);
        }
        LOGGER.info("Git SAML IdP metadata encryption/signing is turned off and "
            + "MAY NOT be safe in a production environment. "
            + "Consider using other choices to handle encryption, signing and verification of "
            + "metadata artifacts");
        return CipherExecutor.noOp();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "gitIdPMetadataRepositoryInstance")
    public GitRepository gitIdPMetadataRepositoryInstance() {
        val git = casProperties.getAuthn().getSamlIdp().getMetadata().getGit();
        return GitRepositoryBuilder.newInstance(git).build();
    }

    @Bean
    @SneakyThrows
    @RefreshScope
    public SamlIdPMetadataGenerator samlIdPMetadataGenerator() {
        val context = SamlIdPMetadataGeneratorConfigurationContext.builder()
            .samlIdPMetadataLocator(samlIdPMetadataLocator())
            .samlIdPCertificateAndKeyWriter(samlSelfSignedCertificateWriter.getObject())
            .applicationContext(applicationContext)
            .casProperties(casProperties)
            .openSamlConfigBean(openSamlConfigBean.getObject())
            .metadataCipherExecutor(gitSamlIdPMetadataCipherExecutor())
            .build();
        return new GitSamlIdPMetadataGenerator(context, gitIdPMetadataRepositoryInstance());
    }

    @Bean
    @SneakyThrows
    @RefreshScope
    public SamlIdPMetadataLocator samlIdPMetadataLocator() {
        return new GitSamlIdPMetadataLocator(gitIdPMetadataRepositoryInstance(), samlIdPMetadataCache.getObject());
    }
}

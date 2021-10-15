package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.git.GitRepositoryBuilder;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.GitSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;

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
 * This is {@link SamlIdPGitRegisteredServiceMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.saml-idp.metadata.git", name = "repository-url")
@Configuration(value = "samlIdPGitRegisteredServiceMetadataConfiguration", proxyBeanMethods = false)
public class SamlIdPGitRegisteredServiceMetadataConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "gitSamlRegisteredServiceRepositoryInstance")
    @Autowired
    public GitRepository gitSamlRegisteredServiceRepositoryInstance(final CasConfigurationProperties casProperties) {
        val git = casProperties.getAuthn().getSamlIdp().getMetadata().getGit();
        return GitRepositoryBuilder.newInstance(git).build();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public SamlRegisteredServiceMetadataResolver gitSamlRegisteredServiceMetadataResolver(final CasConfigurationProperties casProperties,
                                                                                          @Qualifier("gitSamlRegisteredServiceRepositoryInstance")
                                                                                          final GitRepository gitSamlRegisteredServiceRepositoryInstance,
                                                                                          @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
                                                                                          final OpenSamlConfigBean openSamlConfigBean) {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new GitSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean, gitSamlRegisteredServiceRepositoryInstance);
    }

    @Bean
    @ConditionalOnMissingBean(name = "gitSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer gitSamlRegisteredServiceMetadataResolutionPlanConfigurer(
        @Qualifier("gitSamlRegisteredServiceMetadataResolver")
        final SamlRegisteredServiceMetadataResolver gitSamlRegisteredServiceMetadataResolver) {
        return plan -> plan.registerMetadataResolver(gitSamlRegisteredServiceMetadataResolver);
    }
}

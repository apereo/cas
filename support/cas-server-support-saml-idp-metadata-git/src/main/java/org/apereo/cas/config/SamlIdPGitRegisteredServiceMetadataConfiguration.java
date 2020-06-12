package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.git.GitRepository;
import org.apereo.cas.git.GitRepositoryBuilder;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.metadata.resolver.GitSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlanConfigurer;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdPGitRegisteredServiceMetadataConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Configuration("samlIdPGitRegisteredServiceMetadataConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPGitRegisteredServiceMetadataConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "gitRepositoryInstance")
    public GitRepository gitRepositoryInstance() {
        val git = casProperties.getAuthn().getSamlIdp().getMetadata().getGit();
        return GitRepositoryBuilder.newInstance(git).build();
    }

    @Bean
    @RefreshScope
    public SamlRegisteredServiceMetadataResolver gitSamlRegisteredServiceMetadataResolver() {
        val idp = casProperties.getAuthn().getSamlIdp();
        return new GitSamlRegisteredServiceMetadataResolver(idp, openSamlConfigBean.getObject(), gitRepositoryInstance());
    }

    @Bean
    @ConditionalOnMissingBean(name = "gitSamlRegisteredServiceMetadataResolutionPlanConfigurer")
    @RefreshScope
    public SamlRegisteredServiceMetadataResolutionPlanConfigurer gitSamlRegisteredServiceMetadataResolutionPlanConfigurer() {
        return plan -> plan.registerMetadataResolver(gitSamlRegisteredServiceMetadataResolver());
    }
}

package org.apereo.cas.initializr.config;

import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.web.support.InitializrMetadataUpdateStrategy;
import org.apereo.cas.initializr.contrib.ApplicationYamlPropertiesContributor;
import org.apereo.cas.initializr.contrib.ChainingMultipleResourcesProjectContributor;
import org.apereo.cas.initializr.contrib.ChainingSingleResourceProjectContributor;
import org.apereo.cas.initializr.contrib.IgnoreRulesContributor;
import org.apereo.cas.initializr.contrib.OverlayOverrideConfigurationContributor;
import org.apereo.cas.initializr.contrib.OverlaySpringFactoriesContributor;
import org.apereo.cas.initializr.contrib.ProjectAssetsUndoContributor;
import org.apereo.cas.initializr.contrib.ProjectLicenseContributor;
import org.apereo.cas.initializr.contrib.gradle.GradleWrapperConfigurationContributor;
import org.apereo.cas.initializr.contrib.gradle.GradleWrapperExecutablesContributor;
import org.apereo.cas.initializr.info.DependencyAliasesInfoContributor;
import org.apereo.cas.initializr.metadata.CasOverlayInitializrMetadataUpdateStrategy;
import org.apereo.cas.initializr.rate.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ProjectGenerationConfiguration
public class CasInitializrConfiguration {
    @Autowired
    @Bean
    public DependencyAliasesInfoContributor dependencyAliasesInfoContributor(final InitializrMetadataProvider provider) {
        return new DependencyAliasesInfoContributor(provider);
    }

    @Bean
    public ProjectContributor projectLicenseContributor() {
        return new ProjectLicenseContributor();
    }

    @Bean
    public ProjectContributor ignoreRulesContributor() {
        return new IgnoreRulesContributor();
    }

    @Bean
    public ProjectContributor applicationYamlPropertiesContributor() {
        return new ApplicationYamlPropertiesContributor();
    }

    @Bean
    public ChainingMultipleResourcesProjectContributor gradleWrapperContributor() {
        var chain = new ChainingMultipleResourcesProjectContributor();
        chain.addContributor(new GradleWrapperConfigurationContributor());
        chain.addContributor(new GradleWrapperExecutablesContributor());
        return chain;
    }

    @Bean
    public ChainingSingleResourceProjectContributor overlayOverrideSpringConfigContributor() {
        var chain = new ChainingSingleResourceProjectContributor();
        chain.addContributor(new OverlayOverrideConfigurationContributor());
        chain.addContributor(new OverlaySpringFactoriesContributor());
        return chain;
    }

    @Bean
    public InitializrMetadataUpdateStrategy initializrMetadataUpdateStrategy() {
        return new CasOverlayInitializrMetadataUpdateStrategy();
    }

    @Bean
    public ProjectContributor projectAssetsUndoContributor() {
        return new ProjectAssetsUndoContributor();
    }

    @Bean
    public HandlerInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor();
    }

    @Bean
    @Autowired
    public WebMvcConfigurer rateLimitingWebMvcConfigurer(@Qualifier("rateLimitInterceptor")
                                                         final HandlerInterceptor rateLimitInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(final InterceptorRegistry registry) {
                registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/**");
            }
        };
    }
}

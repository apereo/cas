package org.apereo.cas.config;

import org.apereo.cas.overlay.buildsystem.CasOverlayGradleBuild;
import org.apereo.cas.overlay.contrib.CasOverlayAllReferencePropertiesContributor;
import org.apereo.cas.overlay.contrib.CasOverlayApplicationYamlPropertiesContributor;
import org.apereo.cas.overlay.contrib.CasOverlayCasReferencePropertiesContributor;
import org.apereo.cas.overlay.contrib.CasOverlayConfigurationDirectoriesContributor;
import org.apereo.cas.overlay.contrib.CasOverlayConfigurationPropertiesContributor;
import org.apereo.cas.overlay.contrib.CasOverlayIgnoreRulesContributor;
import org.apereo.cas.overlay.contrib.CasOverlayLoggingConfigurationContributor;
import org.apereo.cas.overlay.contrib.CasOverlayOverrideConfigurationContributor;
import org.apereo.cas.overlay.contrib.CasOverlayProjectLicenseContributor;
import org.apereo.cas.overlay.contrib.CasOverlayReadMeContributor;
import org.apereo.cas.overlay.contrib.CasOverlaySpringFactoriesContributor;
import org.apereo.cas.overlay.contrib.CasOverlayWebXmlContributor;
import org.apereo.cas.overlay.contrib.ProjectAssetsUndoContributor;
import org.apereo.cas.overlay.contrib.docker.CasOverlayDockerContributor;
import org.apereo.cas.overlay.contrib.docker.jib.CasOverlayGradleJibContributor;
import org.apereo.cas.overlay.contrib.docker.jib.CasOverlayGradleJibEntrypointContributor;
import org.apereo.cas.overlay.contrib.gradle.CasOverlayGradleBuildContributor;
import org.apereo.cas.overlay.contrib.gradle.CasOverlayGradlePropertiesContributor;
import org.apereo.cas.overlay.contrib.gradle.CasOverlayGradleSettingsContributor;
import org.apereo.cas.overlay.contrib.gradle.CasOverlayGradleSpringBootContributor;
import org.apereo.cas.overlay.contrib.gradle.CasOverlayGradleTasksContributor;
import org.apereo.cas.overlay.contrib.gradle.wrapper.CasOverlayGradleWrapperConfigurationContributor;
import org.apereo.cas.overlay.contrib.gradle.wrapper.CasOverlayGradleWrapperExecutablesContributor;
import org.apereo.cas.overlay.contrib.helm.CasOverlayHelmContributor;
import org.apereo.cas.overlay.contrib.util.ChainingMultipleResourcesProjectContributor;
import org.apereo.cas.overlay.contrib.util.ChainingSingleResourceProjectContributor;
import org.apereo.cas.overlay.customize.DefaultDependenciesBuildCustomizer;
import org.apereo.cas.overlay.info.DependencyAliasesInfoContributor;
import org.apereo.cas.overlay.metadata.CasOverlayInitializrMetadataUpdateStrategy;
import org.apereo.cas.overlay.rate.RateLimitInterceptor;
import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.web.support.InitializrMetadataUpdateStrategy;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.stream.Collectors;

@ProjectGenerationConfiguration
public class CasOverlayProjectGenerationConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Bean
    public DependencyAliasesInfoContributor dependencyAliasesInfoContributor(final InitializrMetadataProvider provider) {
        return new DependencyAliasesInfoContributor(provider);
    }

    @Bean
    public ChainingMultipleResourcesProjectContributor casOverlayGradleWrapperContributor() {
        var chain = new ChainingMultipleResourcesProjectContributor();
        chain.addContributor(new CasOverlayGradleWrapperConfigurationContributor());
        chain.addContributor(new CasOverlayGradleWrapperExecutablesContributor());
        return chain;
    }

    @Bean
    public CasOverlayDockerContributor casOverlayDockerContributor() {
        return new CasOverlayDockerContributor();
    }

    @Bean
    public CasOverlayHelmContributor casOverlayHelmContributor() {
        return new CasOverlayHelmContributor();
    }

    @Bean
    public ChainingSingleResourceProjectContributor casOverlayGradleConfigurationContributor() {
        var chain = new ChainingSingleResourceProjectContributor();
        chain.addContributor(new CasOverlayAllReferencePropertiesContributor(applicationContext));
        chain.addContributor(new CasOverlayCasReferencePropertiesContributor(applicationContext));
        chain.addContributor(new CasOverlayGradleBuildContributor(applicationContext));
        chain.addContributor(new CasOverlayGradlePropertiesContributor(applicationContext));
        chain.addContributor(new CasOverlayConfigurationDirectoriesContributor());
        chain.addContributor(new CasOverlayGradleSettingsContributor());
        chain.addContributor(new CasOverlayGradleSpringBootContributor());
        chain.addContributor(new CasOverlayGradleTasksContributor());
        chain.addContributor(new CasOverlayApplicationYamlPropertiesContributor());
        chain.addContributor(new CasOverlayConfigurationPropertiesContributor(applicationContext));
        chain.addContributor(new CasOverlayOverrideConfigurationContributor());
        chain.addContributor(new CasOverlaySpringFactoriesContributor());
        chain.addContributor(new CasOverlayProjectLicenseContributor());
        chain.addContributor(new CasOverlayWebXmlContributor());
        chain.addContributor(new CasOverlayLoggingConfigurationContributor());
        chain.addContributor(new CasOverlayReadMeContributor(applicationContext));
        chain.addContributor(new CasOverlayIgnoreRulesContributor());
        return chain;
    }

    @Bean
    public ChainingSingleResourceProjectContributor casOverlayJibConfigurationContributor() {
        var chain = new ChainingSingleResourceProjectContributor();
        chain.addContributor(new CasOverlayGradleJibContributor());
        chain.addContributor(new CasOverlayGradleJibEntrypointContributor());
        return chain;
    }

    @Bean
    public CasOverlayGradleBuild gradleBuild(ObjectProvider<BuildCustomizer<CasOverlayGradleBuild>> buildCustomizers,
                                             ObjectProvider<BuildItemResolver> buildItemResolver) {
        var build = new CasOverlayGradleBuild(buildItemResolver.getIfAvailable());
        val customizers = buildCustomizers.orderedStream().collect(Collectors.toList());
        customizers.forEach(c -> c.customize(build));
        return build;
    }

    @Bean
    public BuildCustomizer<CasOverlayGradleBuild> defaultDependenciesBuildCustomizer() {
        return new DefaultDependenciesBuildCustomizer(applicationContext);
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
    public WebMvcConfigurer rateLimitingWebMvcConfigurer(@Qualifier("rateLimitInterceptor") final HandlerInterceptor rateLimitInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(final InterceptorRegistry registry) {
                registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/**");
            }
        };
    }
}

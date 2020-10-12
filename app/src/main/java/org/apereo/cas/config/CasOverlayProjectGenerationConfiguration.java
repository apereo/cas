package org.apereo.cas.config;

import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import org.apereo.cas.overlay.CasOverlayGradleBuild;
import org.apereo.cas.overlay.contrib.CasOverlayLoggingConfigurationContributor;
import org.apereo.cas.overlay.contrib.CasOverlayProjectLicenseContributor;
import org.apereo.cas.overlay.contrib.CasOverlayReadMeContributor;
import org.apereo.cas.overlay.contrib.CasOverlayWebXmlContributor;
import org.apereo.cas.overlay.contrib.util.ChainingMultipleResourcesProjectContributor;
import org.apereo.cas.overlay.contrib.util.ChainingSingleResourceProjectContributor;
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
import org.apereo.cas.overlay.contrib.CasApplicationYamlPropertiesContributor;
import org.apereo.cas.overlay.contrib.CasOverlayOverrideConfigurationContributor;
import org.apereo.cas.overlay.contrib.CasOverlaySpringFactoriesContributor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@ProjectGenerationConfiguration
public class CasOverlayProjectGenerationConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private static CasOverlayGradleBuild createGradleBuild(BuildItemResolver buildItemResolver) {
        return buildItemResolver != null ? new CasOverlayGradleBuild(buildItemResolver) : new CasOverlayGradleBuild();
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
    public ChainingSingleResourceProjectContributor casOverlayGradleConfigurationContributor() {
        var chain = new ChainingSingleResourceProjectContributor();
        chain.addContributor(new CasOverlayGradleBuildContributor(applicationContext));
        chain.addContributor(new CasOverlayGradlePropertiesContributor(applicationContext));
        chain.addContributor(new CasOverlayGradleSettingsContributor());
        chain.addContributor(new CasOverlayGradleSpringBootContributor());
        chain.addContributor(new CasOverlayGradleTasksContributor());
        chain.addContributor(new CasApplicationYamlPropertiesContributor());
        chain.addContributor(new CasOverlayOverrideConfigurationContributor());
        chain.addContributor(new CasOverlaySpringFactoriesContributor());
        chain.addContributor(new CasOverlayProjectLicenseContributor());
        chain.addContributor(new CasOverlayWebXmlContributor());
        chain.addContributor(new CasOverlayLoggingConfigurationContributor());
        chain.addContributor(new CasOverlayReadMeContributor());
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
    public CasOverlayGradleBuild gradleBuild(ObjectProvider<BuildItemResolver> buildItemResolver) {
        return createGradleBuild(buildItemResolver.getIfAvailable());
    }

}

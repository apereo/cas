package org.apereo.cas.config;

import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import org.apereo.cas.overlay.contrib.CasOverlayConfigurationContributor;
import org.apereo.cas.overlay.contrib.gradle.wrapper.CasOverlayGradleWrapperConfigurationContributor;
import org.apereo.cas.overlay.CasOverlayGradleBuild;
import org.apereo.cas.overlay.contrib.CasOverlayReadMeContributor;
import org.apereo.cas.overlay.contrib.ChainingMultipleResourcesProjectContributor;
import org.apereo.cas.overlay.contrib.gradle.wrapper.CasOverlayGradleWrapperExecutablesContributor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.stream.Collectors;

@ProjectGenerationConfiguration
public class CasOverlayProjectGenerationConfiguration {

    private static CasOverlayGradleBuild createGradleBuild(BuildItemResolver buildItemResolver,
                                                           List<BuildCustomizer<?>> buildCustomizers) {
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
    public CasOverlayReadMeContributor casOverlayReadMeContributor() {
        return new CasOverlayReadMeContributor();
    }

    @Bean
    public CasOverlayConfigurationContributor casOverlayConfigurationContributor() {
        return new CasOverlayConfigurationContributor();
    }
    
    @Bean
    public CasOverlayGradleBuild gradleBuild(ObjectProvider<BuildItemResolver> buildItemResolver,
                                             ObjectProvider<BuildCustomizer<?>> buildCustomizers) {
        return createGradleBuild(buildItemResolver.getIfAvailable(),
            buildCustomizers.orderedStream().collect(Collectors.toList()));
    }

}

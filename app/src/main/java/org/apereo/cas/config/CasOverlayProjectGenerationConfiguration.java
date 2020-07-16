package org.apereo.cas.config;

import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import org.apereo.cas.overlay.CasGradleWrapperContributor;
import org.apereo.cas.overlay.CasOverlayBuildSystem;
import org.apereo.cas.overlay.CasOverlayGradleBuild;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.util.LambdaSafe;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.stream.Collectors;

@ProjectGenerationConfiguration
//@ConditionalOnBuildSystem(CasOverlayBuildSystem.ID)
public class CasOverlayProjectGenerationConfiguration {

    private static CasOverlayGradleBuild createGradleBuild(BuildItemResolver buildItemResolver, List<BuildCustomizer<?>> buildCustomizers) {
        var build = buildItemResolver != null ? new CasOverlayGradleBuild(buildItemResolver) : new CasOverlayGradleBuild();
        LambdaSafe.callbacks(BuildCustomizer.class, buildCustomizers, build)
            .invoke(customizer -> customizer.customize(build));
        return build;
    }
    
    @Bean
    public CasGradleWrapperContributor gradleWrapperContributor() {
        return new CasGradleWrapperContributor();
    }

    @Bean
    public CasOverlayGradleBuild gradleBuild(ObjectProvider<BuildItemResolver> buildItemResolver,
                                             ObjectProvider<BuildCustomizer<?>> buildCustomizers) {
        return createGradleBuild(buildItemResolver.getIfAvailable(),
            buildCustomizers.orderedStream().collect(Collectors.toList()));
    }

}

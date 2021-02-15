package org.apereo.cas.overlay.discoveryserver.config;

import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import lombok.val;
import org.apereo.cas.initializr.contrib.ChainingSingleResourceProjectContributor;
import org.apereo.cas.overlay.discoveryserver.buildsystem.CasDiscoveryServerOverlayBuildSystem;
import org.apereo.cas.overlay.discoveryserver.buildsystem.CasDiscoveryServerOverlayGradleBuild;
import org.apereo.cas.overlay.discoveryserver.contrib.CasDiscoveryServerOverlayGradleBuildContributor;
import org.apereo.cas.overlay.discoveryserver.contrib.CasDiscoveryServerOverlayGradlePropertiesContributor;
import org.apereo.cas.overlay.discoveryserver.contrib.CasDiscoveryServerOverlayGradleSettingsContributor;
import org.apereo.cas.overlay.discoveryserver.contrib.CasDiscoveryServerOverlayReadMeContributor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.stream.Collectors;

@ProjectGenerationConfiguration
@ConditionalOnBuildSystem(CasDiscoveryServerOverlayBuildSystem.ID)
public class CasDiscoveryServerOverlayProjectGenerationConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Bean
    public ChainingSingleResourceProjectContributor discoveryOverlayGradleConfigurationContributor() {
        var chain = new ChainingSingleResourceProjectContributor();
        chain.addContributor(new CasDiscoveryServerOverlayGradleBuildContributor());
        chain.addContributor(new CasDiscoveryServerOverlayGradleSettingsContributor());
        chain.addContributor(new CasDiscoveryServerOverlayGradlePropertiesContributor(applicationContext));
        chain.addContributor(new CasDiscoveryServerOverlayReadMeContributor(applicationContext));
        return chain;
    }

    @Bean
    public CasDiscoveryServerOverlayGradleBuild gradleBuild(ObjectProvider<BuildCustomizer<CasDiscoveryServerOverlayGradleBuild>> buildCustomizers,
                                                            ObjectProvider<BuildItemResolver> buildItemResolver) {
        var build = new CasDiscoveryServerOverlayGradleBuild(buildItemResolver.getIfAvailable());
        val customizers = buildCustomizers.orderedStream().collect(Collectors.toList());
        customizers.forEach(c -> c.customize(build));
        return build;
    }
}

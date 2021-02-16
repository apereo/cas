package org.apereo.cas.overlay.bootadminserver.config;

import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import lombok.val;
import org.apereo.cas.initializr.contrib.ChainingSingleResourceProjectContributor;
import org.apereo.cas.overlay.bootadminserver.buildsystem.CasSpringBootAdminServerOverlayBuildSystem;
import org.apereo.cas.overlay.bootadminserver.buildsystem.CasSpringBootAdminServerOverlayGradleBuild;
import org.apereo.cas.overlay.bootadminserver.contrib.CasSpringBootAdminServerOverlayGradleBuildContributor;
import org.apereo.cas.overlay.bootadminserver.contrib.CasSpringBootAdminServerOverlayGradlePropertiesContributor;
import org.apereo.cas.overlay.bootadminserver.contrib.CasSpringBootAdminServerOverlayGradleSettingsContributor;
import org.apereo.cas.overlay.bootadminserver.contrib.CasSpringBootAdminServerOverlayReadMeContributor;
import org.apereo.cas.overlay.bootadminserver.contrib.docker.jib.CasSpringBootAdminGradleJibEntrypointContributor;
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
@ConditionalOnBuildSystem(CasSpringBootAdminServerOverlayBuildSystem.ID)
public class CasSpringBootAdminServerOverlayProjectGenerationConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Bean
    public ChainingSingleResourceProjectContributor bootAdminOverlayGradleConfigurationContributor() {
        var chain = new ChainingSingleResourceProjectContributor();
        chain.addContributor(new CasSpringBootAdminGradleJibEntrypointContributor());
        chain.addContributor(new CasSpringBootAdminServerOverlayGradleBuildContributor());
        chain.addContributor(new CasSpringBootAdminServerOverlayGradleSettingsContributor());
        chain.addContributor(new CasSpringBootAdminServerOverlayGradlePropertiesContributor(applicationContext));
        chain.addContributor(new CasSpringBootAdminServerOverlayReadMeContributor(applicationContext));
        return chain;
    }

    @Bean
    public CasSpringBootAdminServerOverlayGradleBuild gradleBuild(ObjectProvider<BuildCustomizer<CasSpringBootAdminServerOverlayGradleBuild>> buildCustomizers,
                                                                  ObjectProvider<BuildItemResolver> buildItemResolver) {
        var build = new CasSpringBootAdminServerOverlayGradleBuild(buildItemResolver.getIfAvailable());
        val customizers = buildCustomizers.orderedStream().collect(Collectors.toList());
        customizers.forEach(c -> c.customize(build));
        return build;
    }
}

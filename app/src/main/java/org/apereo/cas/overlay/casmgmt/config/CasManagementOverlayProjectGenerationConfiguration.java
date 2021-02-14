package org.apereo.cas.overlay.casmgmt.config;

import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import lombok.val;
import org.apereo.cas.initializr.contrib.ChainingSingleResourceProjectContributor;
import org.apereo.cas.overlay.casmgmt.buildsystem.CasManagementOverlayBuildSystem;
import org.apereo.cas.overlay.casmgmt.buildsystem.CasManagementOverlayGradleBuild;
import org.apereo.cas.overlay.casmgmt.contrib.CasManagementOverlayConfigurationPropertiesContributor;
import org.apereo.cas.overlay.casmgmt.contrib.CasManagementOverlayGradleBuildContributor;
import org.apereo.cas.overlay.casmgmt.contrib.CasManagementOverlayGradlePropertiesContributor;
import org.apereo.cas.overlay.casmgmt.contrib.CasManagementOverlayGradleSettingsContributor;
import org.apereo.cas.overlay.casmgmt.contrib.CasManagementOverlayLoggingConfigurationContributor;
import org.apereo.cas.overlay.casmgmt.contrib.CasManagementOverlayReadMeContributor;
import org.apereo.cas.overlay.casmgmt.contrib.CasManagementOverlayUsersConfigurationContributor;
import org.apereo.cas.overlay.casmgmt.contrib.docker.CasManagementOverlayDockerContributor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.stream.Collectors;

@ProjectGenerationConfiguration
@ConditionalOnBuildSystem(CasManagementOverlayBuildSystem.ID)
public class CasManagementOverlayProjectGenerationConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Bean
    public CasManagementOverlayDockerContributor casMgmtOverlayDockerContributor() {
        return new CasManagementOverlayDockerContributor();
    }

    @Bean
    public ChainingSingleResourceProjectContributor casMgmtOverlayGradleConfigurationContributor() {
        var chain = new ChainingSingleResourceProjectContributor();
        chain.addContributor(new CasManagementOverlayGradleBuildContributor(applicationContext));
        chain.addContributor(new CasManagementOverlayGradlePropertiesContributor(applicationContext));
        chain.addContributor(new CasManagementOverlayGradleSettingsContributor());
        chain.addContributor(new CasManagementOverlayReadMeContributor(applicationContext));
        return chain;
    }

    @Bean
    public ChainingSingleResourceProjectContributor casMgmtOverlayConfigurationContributor() {
        var chain = new ChainingSingleResourceProjectContributor();
        chain.addContributor(new CasManagementOverlayConfigurationPropertiesContributor(applicationContext));
        chain.addContributor(new CasManagementOverlayLoggingConfigurationContributor());
        chain.addContributor(new CasManagementOverlayUsersConfigurationContributor());
        return chain;
    }

    @Bean
    public CasManagementOverlayGradleBuild gradleBuild(ObjectProvider<BuildCustomizer<CasManagementOverlayGradleBuild>> buildCustomizers,
                                                       ObjectProvider<BuildItemResolver> buildItemResolver) {
        var build = new CasManagementOverlayGradleBuild(buildItemResolver.getIfAvailable());
        val customizers = buildCustomizers.orderedStream().collect(Collectors.toList());
        customizers.forEach(c -> c.customize(build));
        return build;
    }
}

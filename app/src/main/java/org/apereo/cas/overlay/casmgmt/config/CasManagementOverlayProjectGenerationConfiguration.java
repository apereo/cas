package org.apereo.cas.overlay.casmgmt.config;

import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import lombok.val;
import org.apereo.cas.initializr.contrib.ApplicationYamlPropertiesContributor;
import org.apereo.cas.initializr.contrib.ChainingSingleResourceProjectContributor;
import org.apereo.cas.initializr.contrib.IgnoreRulesContributor;
import org.apereo.cas.initializr.contrib.ProjectLicenseContributor;
import org.apereo.cas.overlay.casmgmt.buildsystem.CasManagementOverlayBuildSystem;
import org.apereo.cas.overlay.casmgmt.buildsystem.CasManagementOverlayGradleBuild;
import org.apereo.cas.overlay.casserver.buildsystem.CasOverlayBuildSystem;
import org.apereo.cas.overlay.casserver.buildsystem.CasOverlayGradleBuild;
import org.apereo.cas.overlay.casserver.contrib.CasOverlayAllReferencePropertiesContributor;
import org.apereo.cas.overlay.casserver.contrib.CasOverlayCasReferencePropertiesContributor;
import org.apereo.cas.overlay.casserver.contrib.CasOverlayConfigurationDirectoriesContributor;
import org.apereo.cas.overlay.casserver.contrib.CasOverlayConfigurationPropertiesContributor;
import org.apereo.cas.overlay.casserver.contrib.CasOverlayLoggingConfigurationContributor;
import org.apereo.cas.overlay.casserver.contrib.CasOverlayOverrideConfigurationContributor;
import org.apereo.cas.overlay.casserver.contrib.CasOverlayReadMeContributor;
import org.apereo.cas.overlay.casserver.contrib.CasOverlaySpringFactoriesContributor;
import org.apereo.cas.overlay.casserver.contrib.CasOverlayWebXmlContributor;
import org.apereo.cas.overlay.casserver.contrib.docker.CasOverlayDockerContributor;
import org.apereo.cas.overlay.casserver.contrib.docker.jib.CasOverlayGradleJibContributor;
import org.apereo.cas.overlay.casserver.contrib.docker.jib.CasOverlayGradleJibEntrypointContributor;
import org.apereo.cas.overlay.casserver.contrib.gradle.CasOverlayGradleBuildContributor;
import org.apereo.cas.overlay.casserver.contrib.gradle.CasOverlayGradlePropertiesContributor;
import org.apereo.cas.overlay.casserver.contrib.gradle.CasOverlayGradleSettingsContributor;
import org.apereo.cas.overlay.casserver.contrib.gradle.CasOverlayGradleSpringBootContributor;
import org.apereo.cas.overlay.casserver.contrib.gradle.CasOverlayGradleTasksContributor;
import org.apereo.cas.overlay.casserver.contrib.helm.CasOverlayHelmContributor;
import org.apereo.cas.overlay.casserver.customize.DefaultDependenciesBuildCustomizer;
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
    public CasManagementOverlayGradleBuild gradleBuild(ObjectProvider<BuildCustomizer<CasManagementOverlayGradleBuild>> buildCustomizers,
                                                       ObjectProvider<BuildItemResolver> buildItemResolver) {
        var build = new CasManagementOverlayGradleBuild(buildItemResolver.getIfAvailable());
        val customizers = buildCustomizers.orderedStream().collect(Collectors.toList());
        customizers.forEach(c -> c.customize(build));
        return build;
    }
}

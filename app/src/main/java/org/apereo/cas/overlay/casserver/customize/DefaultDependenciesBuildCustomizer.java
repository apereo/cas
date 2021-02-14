package org.apereo.cas.overlay.casserver.customize;

import org.apereo.cas.overlay.casserver.buildsystem.CasOverlayGradleBuild;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

@RequiredArgsConstructor
public class DefaultDependenciesBuildCustomizer implements BuildCustomizer<CasOverlayGradleBuild> {
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void customize(final CasOverlayGradleBuild build) {
        if (!applicationContext.getBeansOfType(ProjectDescription.class).isEmpty()) {
            build.dependencies().add("core");
            build.dependencies().add("core-audit");
            build.dependencies().add("core-authentication");
            build.dependencies().add("core-configuration");
            build.dependencies().add("core-cookie");
            build.dependencies().add("core-logout");
            build.dependencies().add("core-logging");
            build.dependencies().add("core-services");
            build.dependencies().add("core-tickets");
            build.dependencies().add("core-util");
            build.dependencies().add("core-validation");
            build.dependencies().add("core-web");
            build.dependencies().add("core-notifications");

            build.dependencies().add("support-actions");
            build.dependencies().add("support-jpa-util");
            build.dependencies().add("support-person-directory");
            build.dependencies().add("support-themes");
            build.dependencies().add("support-validation");
            build.dependencies().add("support-thymeleaf");
            build.dependencies().add("support-pm-webflow");

            build.dependencies().add("webapp-config");
            build.dependencies().add("webapp-init");
            build.dependencies().add("webapp-resources");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

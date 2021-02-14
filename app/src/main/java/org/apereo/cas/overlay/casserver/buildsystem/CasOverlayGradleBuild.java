package org.apereo.cas.overlay.casserver.buildsystem;

import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.buildsystem.BuildSettings;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSettings;
import java.util.List;

public class CasOverlayGradleBuild extends Build {
    public static final List<String> WEBAPP_ARTIFACTS =
        List.of("cas-server-webapp-tomcat", "cas-server-webapp-jetty", "cas-server-webapp-undertow");

    private final GradleBuildSettings.Builder settings = new GradleBuildSettings.Builder();

    public CasOverlayGradleBuild(final BuildItemResolver buildItemResolver) {
        super(buildItemResolver);
    }

    @Override
    public BuildSettings.Builder<?> settings() {
        return settings;
    }

    @Override
    public BuildSettings getSettings() {
        return settings.build();
    }
}

package org.apereo.cas.overlay.bootadminserver.buildsystem;

import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.buildsystem.BuildSettings;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSettings;

public class CasSpringBootAdminServerOverlayGradleBuild extends Build {
    private final GradleBuildSettings.Builder settings = new GradleBuildSettings.Builder();

    public CasSpringBootAdminServerOverlayGradleBuild(final BuildItemResolver buildItemResolver) {
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

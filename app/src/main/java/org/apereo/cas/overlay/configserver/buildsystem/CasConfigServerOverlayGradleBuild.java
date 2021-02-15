package org.apereo.cas.overlay.configserver.buildsystem;

import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.buildsystem.BuildSettings;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSettings;

import java.util.List;

public class CasConfigServerOverlayGradleBuild extends Build {
    private final GradleBuildSettings.Builder settings = new GradleBuildSettings.Builder();

    public CasConfigServerOverlayGradleBuild(final BuildItemResolver buildItemResolver) {
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

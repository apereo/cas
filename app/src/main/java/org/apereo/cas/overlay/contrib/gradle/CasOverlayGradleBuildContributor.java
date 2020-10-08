package org.apereo.cas.overlay.contrib.gradle;

import io.spring.initializr.generator.project.contributor.SingleResourceProjectContributor;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.nio.file.Path;

@Setter
public class CasOverlayGradleBuildContributor extends SingleResourceProjectContributor implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    public CasOverlayGradleBuildContributor() {
        super("./build.gradle", "classpath:overlay/build.gradle");

    }
}

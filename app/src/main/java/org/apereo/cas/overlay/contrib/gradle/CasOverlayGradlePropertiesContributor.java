package org.apereo.cas.overlay.contrib.gradle;

import org.apereo.cas.overlay.contrib.util.TemplatedProjectContributor;
import io.spring.initializr.generator.project.ProjectDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;

@Slf4j
public class CasOverlayGradlePropertiesContributor extends TemplatedProjectContributor {
    public CasOverlayGradlePropertiesContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./gradle.properties", "classpath:overlay/gradle.properties");
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        val dependencies = project.getRequestedDependencies();
        var appServer = "-tomcat";
        if (dependencies.containsKey("webapp-jetty")) {
            appServer = "-jetty";
        } else if (dependencies.containsKey("webapp-undertow")) {
            appServer = "-undertow";
        }
        return new CasPropertiesContainer(new CasProperties(appServer));
    }

    @RequiredArgsConstructor
    private static class CasPropertiesContainer {
        private final CasProperties properties;

        CasProperties properties() {
            return this.properties;
        }
    }

    @Getter
    @AllArgsConstructor
    @ToString
    private static class CasProperties {
        private final String appServer;
    }
}

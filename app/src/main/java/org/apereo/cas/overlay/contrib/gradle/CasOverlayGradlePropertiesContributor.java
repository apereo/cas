package org.apereo.cas.overlay.contrib.gradle;

import org.apereo.cas.overlay.contrib.util.TemplatedProjectContributor;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
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
        val provider = applicationContext.getBean(InitializrMetadataProvider.class);
        val defaults = provider.get().defaults();

        val dependencies = project.getRequestedDependencies();
        var appServer = "-tomcat";
        if (dependencies.containsKey("webapp-jetty")) {
            appServer = "-jetty";
        } else if (dependencies.containsKey("webapp-undertow")) {
            appServer = "-undertow";
        }
        defaults.put("appServer", appServer);
        return defaults;
    }

    @Getter
    @SuperBuilder
    @ToString
    private static class CasProperties {
        private final String appServer;
    }
}

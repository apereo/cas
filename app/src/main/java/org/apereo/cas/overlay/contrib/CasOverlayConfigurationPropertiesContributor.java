package org.apereo.cas.overlay.contrib;

import org.apereo.cas.overlay.contrib.util.TemplatedProjectContributor;
import io.spring.initializr.generator.project.ProjectDescription;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import java.util.List;

public class CasOverlayConfigurationPropertiesContributor extends TemplatedProjectContributor {
    public CasOverlayConfigurationPropertiesContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./etc/cas/config/cas.properties", "classpath:/overlay/etc/cas/config/cas.properties");
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        return new CasPropertiesContainer(List.of());
    }

    @RequiredArgsConstructor
    public static class CasPropertiesContainer {
        private final List<CasProperty> properties;

        List<CasProperty> properties() {
            return this.properties;
        }
    }

    @RequiredArgsConstructor
    public static class CasProperty {
        private final String key;

        private final String value;
    }
}

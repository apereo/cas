package org.apereo.cas.overlay.contrib;

import org.apereo.cas.overlay.contrib.util.CasOverlayPropertiesCatalog;
import org.apereo.cas.overlay.contrib.util.TemplatedProjectContributor;
import io.spring.initializr.generator.project.ProjectDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public class CasOverlayOtherReferencePropertiesContributor extends TemplatedProjectContributor {
    public CasOverlayOtherReferencePropertiesContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./etc/cas/config/all-other-properties.ref",
            "classpath:/overlay/etc/cas/config/all-other-properties.ref");
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        return CasOverlayPropertiesCatalog.builder().casExclusive(false).build().catalog();
    }
}

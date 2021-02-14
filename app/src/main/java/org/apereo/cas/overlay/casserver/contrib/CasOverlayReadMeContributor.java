package org.apereo.cas.overlay.casserver.contrib;

import org.apereo.cas.initializr.contrib.TemplatedProjectContributor;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class CasOverlayReadMeContributor extends TemplatedProjectContributor {
    public CasOverlayReadMeContributor(final ApplicationContext applicationContext) {
        super(applicationContext, "./README.md", "classpath:overlay/README.md");
    }

    protected static String generateAppUrl() {
        var builder = ServletUriComponentsBuilder.fromCurrentServletMapping();
        builder.scheme("https");
        return builder.build().toString();
    }

    @Override
    protected Object contributeInternal(final ProjectDescription project) {
        val provider = applicationContext.getBean(InitializrMetadataProvider.class);
        val output = provider.get().defaults();
        output.put("initializrUrl", generateAppUrl());
        return output;
    }
}

package org.apereo.cas.overlay.metadata;

import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.web.support.InitializrMetadataUpdateStrategy;

public class CasOverlayInitializrMetadataUpdateStrategy implements InitializrMetadataUpdateStrategy {
    @Override
    public InitializrMetadata update(final InitializrMetadata current) {
        return current;
    }
}

package org.apereo.cas.overlay.discoveryserver.buildsystem;

import io.spring.initializr.generator.buildsystem.BuildSystem;

public class CasDiscoveryServerOverlayBuildSystem implements BuildSystem {
    public static final String ID = "cas-discovery-server-overlay";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String toString() {
        return id();
    }
}

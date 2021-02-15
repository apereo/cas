package org.apereo.cas.overlay.configserver.buildsystem;

import io.spring.initializr.generator.buildsystem.BuildSystem;

public class CasConfigServerOverlayBuildSystem implements BuildSystem {
    public static final String ID = "cas-config-server-overlay";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String toString() {
        return id();
    }
}

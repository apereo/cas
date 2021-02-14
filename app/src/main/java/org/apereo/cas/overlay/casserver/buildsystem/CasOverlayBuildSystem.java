package org.apereo.cas.overlay.casserver.buildsystem;

import io.spring.initializr.generator.buildsystem.BuildSystem;

public class CasOverlayBuildSystem implements BuildSystem {
    public static final String ID = "cas-overlay";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String toString() {
        return id();
    }
}

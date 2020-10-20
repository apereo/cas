package org.apereo.cas.overlay.buildsystem;

import io.spring.initializr.generator.buildsystem.BuildSystem;

public class CasOverlayBuildSystem implements BuildSystem {
    public static final String ID = "overlay";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String toString() {
        return id();
    }
}

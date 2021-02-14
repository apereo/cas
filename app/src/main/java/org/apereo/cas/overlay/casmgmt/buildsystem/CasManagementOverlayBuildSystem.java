package org.apereo.cas.overlay.casmgmt.buildsystem;

import io.spring.initializr.generator.buildsystem.BuildSystem;

public class CasManagementOverlayBuildSystem implements BuildSystem {
    public static final String ID = "cas-mgmt-overlay";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String toString() {
        return id();
    }
}

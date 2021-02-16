package org.apereo.cas.overlay.bootadminserver.buildsystem;

import io.spring.initializr.generator.buildsystem.BuildSystem;

public class CasSpringBootAdminServerOverlayBuildSystem implements BuildSystem {
    public static final String ID = "cas-bootadmin-server-overlay";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String toString() {
        return id();
    }
}

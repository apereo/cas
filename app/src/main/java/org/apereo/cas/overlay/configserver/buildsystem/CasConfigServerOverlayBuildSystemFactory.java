package org.apereo.cas.overlay.configserver.buildsystem;

import io.spring.initializr.generator.buildsystem.BuildSystem;
import io.spring.initializr.generator.buildsystem.BuildSystemFactory;

public class CasConfigServerOverlayBuildSystemFactory implements BuildSystemFactory {

    @Override
    public BuildSystem createBuildSystem(String id) {
        return createBuildSystem(id, null);
    }

    @Override
    public BuildSystem createBuildSystem(String id, String dialect) {
        if (CasConfigServerOverlayBuildSystem.ID.equals(id)) {
            return new CasConfigServerOverlayBuildSystem();
        }
        return null;
    }

}

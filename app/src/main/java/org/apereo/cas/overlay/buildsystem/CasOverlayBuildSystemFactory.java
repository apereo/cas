package org.apereo.cas.overlay.buildsystem;

import io.spring.initializr.generator.buildsystem.BuildSystem;
import io.spring.initializr.generator.buildsystem.BuildSystemFactory;

public class CasOverlayBuildSystemFactory implements BuildSystemFactory {

    @Override
    public BuildSystem createBuildSystem(String id) {
        return createBuildSystem(id, null);
    }

    @Override
    public BuildSystem createBuildSystem(String id, String dialect) {
        if (CasOverlayBuildSystem.ID.equals(id)) {
            return new CasOverlayBuildSystem();
        }
        return null;
    }

}

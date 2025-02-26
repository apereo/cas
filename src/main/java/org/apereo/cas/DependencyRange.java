package org.apereo.cas;

import com.vdurmont.semver4j.Semver;

public record DependencyRange(Semver startingVersion, Semver endingVersion) {
    public boolean isQualifiedForPatchUpgrade() {
        return startingVersion.getMajor().equals(endingVersion.getMajor())
            && startingVersion.getMinor().equals(endingVersion.getMinor())
            && endingVersion.getPatch() > startingVersion.getPatch();
    }

    public boolean isQualifiedForMinorUpgrade() {
        return startingVersion.getMajor().equals(endingVersion.getMajor())
            && endingVersion.getMinor() > startingVersion.getMinor();
    }

    public boolean isQualifiedForMajorUpgrade() {
        return endingVersion.getMajor() > startingVersion.getMajor();
    }
}

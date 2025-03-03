package org.apereo.cas;


import org.semver4j.Semver;

public record DependencyRange(Semver startingVersion, Semver endingVersion) {

    public boolean isQualifiedForPatchUpgrade() {
        var patchUpdate = startingVersion.getMajor() == endingVersion.getMajor()
            && startingVersion.getMinor() == endingVersion.getMinor()
            && endingVersion.getPatch() > startingVersion.getPatch();
        return patchUpdate || startingVersion.equals(endingVersion);
    }

    public boolean isQualifiedForMinorUpgrade() {
        return startingVersion.getMajor() == endingVersion.getMajor()
            && endingVersion.getMinor() > startingVersion.getMinor();
    }

    public boolean isQualifiedForMajorUpgrade() {
        return endingVersion.getMajor() > startingVersion.getMajor();
    }
}

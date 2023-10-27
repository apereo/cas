package org.apereo.cas.util;

import lombok.experimental.UtilityClass;

/**
 * Class that exposes the CAS version. Fetches the "Implementation-Version"
 * manifest attribute from the jar file.
 *
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
@UtilityClass
public class CasVersion {
    /**
     * To string.
     *
     * @return the string
     */
    public static String asString() {
        return getVersion() + " - " + getSpecificationVersion();
    }

    /**
     * The full CAS version string.
     *
     * @return Return the full CAS version string.
     * @see Package#getImplementationVersion
     */
    public static String getVersion() {
        return CasVersion.class.getPackage().getImplementationVersion();
    }

    /**
     * Gets specification version from the manifest package.
     *
     * @return the specification version
     */
    public static String getSpecificationVersion() {
        return CasVersion.class.getPackage().getSpecificationVersion();
    }
}

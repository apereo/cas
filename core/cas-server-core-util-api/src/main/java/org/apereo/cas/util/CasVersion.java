package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;


/**
 * Class that exposes the CAS version. Fetches the "Implementation-Version"
 * manifest attribute from the jar file.
 *
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
@Slf4j
@UtilityClass
public class CasVersion {
    /**
     * To string.
     *
     * @return the string
     */
    public static String asString() {
        return getVersion() + " - " + getSpecificationVersion() + " - " + getDateTime().toString();
    }

    /**
     * @return Return the full CAS version string.
     * @see java.lang.Package#getImplementationVersion
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

    /**
     * Gets last modified date/time for the module.
     *
     * @return the date/time
     */
    public static ZonedDateTime getDateTime() {
        val clazz = CasVersion.class;
        val resource = clazz.getResource(clazz.getSimpleName() + ".class");
        try {
            val time = Instant.ofEpochMilli(resource.openConnection().getLastModified());
            return DateTimeUtils.zonedDateTimeOf(time);
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        LOGGER.warn("Unhandled url protocol: [{}] resource: [{}]", resource.getProtocol(), resource);
        return ZonedDateTime.now(ZoneOffset.UTC);
    }
}

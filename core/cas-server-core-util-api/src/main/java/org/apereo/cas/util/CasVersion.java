package org.apereo.cas.util;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.net.URI;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.jar.Manifest;

/**
 * Class that exposes the CAS version. Fetches the "Implementation-Version"
 * manifest attribute from the jar file.
 *
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
@UtilityClass
@SuppressWarnings({"CatchAndPrintStackTrace", "TimeInStaticInitializer"})
public class CasVersion {
    private static final String IMPLEMENTATION_DATE;

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

    public static ZonedDateTime getDateTime() {
        return IMPLEMENTATION_DATE != null ? DateTimeUtils.zonedDateTimeOf(IMPLEMENTATION_DATE) : ZonedDateTime.now(Clock.systemUTC());
    }

    static {
        IMPLEMENTATION_DATE = CasRuntimeHintsRegistrar.inNativeImage()
            ? ZonedDateTime.now(Clock.systemUTC()).toString()
            : FunctionUtils.doAndHandle(() -> {
                val className = CasVersion.class.getSimpleName() + ".class";
                val classPath = CasVersion.class.getResource(className).toString();
                val manifestPath = classPath.substring(0, classPath.lastIndexOf('!') + 1) + "/META-INF/MANIFEST.MF";
                try (val url = URI.create(manifestPath).toURL().openStream()) {
                    val manifest = new Manifest(url);
                    val attributes = manifest.getMainAttributes();
                    return StringUtils.defaultIfBlank(attributes.getValue("Implementation-Date"), ZonedDateTime.now(Clock.systemUTC()).toString());
                }
            });
    }
}

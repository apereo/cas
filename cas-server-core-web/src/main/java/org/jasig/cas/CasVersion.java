package org.jasig.cas;

import org.jasig.cas.util.DateTimeUtils;

import java.io.File;
import java.net.URL;
import java.time.ZonedDateTime;

/**
 * Class that exposes the CAS version. Fetches the "Implementation-Version"
 * manifest attribute from the jar file.
 *
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
public class CasVersion {

    /**
     * Private constructor for CasVersion. You should not be able to instantiate
     * this class.
     */
    private CasVersion() {
        // this class is not instantiable
    }

    /**
     * @return Return the full CAS version string.
     * @see java.lang.Package#getImplementationVersion
     */
    public static String getVersion() {
        return CasVersion.class.getPackage().getImplementationVersion();
    }

    /**
     * Gets last modified date/time for the module.
     * @return the date/time
     */
    public static ZonedDateTime getDateTime() {
        try {
            final Class clazz = CasVersion.class;
            final URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
            if ("file".equals(resource.getProtocol())) {
                return DateTimeUtils.zonedDateTimeOf(new File(resource.toURI()).lastModified());
            } 
            if ("jar".equals(resource.getProtocol())) {
                final String path = resource.getPath();
                final File file = new File(path.substring(5, path.indexOf('!')));
                return DateTimeUtils.zonedDateTimeOf(file.lastModified());
            }
            throw new IllegalArgumentException("Unhandled url protocol: "
                        + resource.getProtocol() + " for class: "
                        + clazz.getName() + " resource: " + resource);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}

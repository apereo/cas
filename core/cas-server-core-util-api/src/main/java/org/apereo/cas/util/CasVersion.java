package org.apereo.cas.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.VfsResource;

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
@Slf4j
@UtilityClass
public class CasVersion {

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
    @SneakyThrows
    public static ZonedDateTime getDateTime() {
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
        if ("vfs".equals(resource.getProtocol())) {
            final File file = new VfsResource(resource.openConnection().getContent()).getFile();
            return DateTimeUtils.zonedDateTimeOf(file.lastModified());
        }
        LOGGER.warn("Unhandled url protocol: [{}] resource: [{}]", resource.getProtocol(), resource);
        return ZonedDateTime.now();
    }
}

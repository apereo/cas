package org.jasig.cas.util;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Utility class to assist with resource operations.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public final class ResourceUtils {

    private ResourceUtils() {}

    /**
     * Gets resource from a String location.
     *
     * @param metadataLocation the metadata location
     * @return the resource from
     * @throws IOException the exception
     */
    public static AbstractResource getResourceFrom(final String metadataLocation) throws IOException {
        final AbstractResource metadataLocationResource;
        if (metadataLocation.toLowerCase().startsWith("http")) {
            metadataLocationResource = new UrlResource(metadataLocation);
        } else if (metadataLocation.toLowerCase().startsWith("classpath")) {
            metadataLocationResource = new ClassPathResource(metadataLocation);
        } else {
            metadataLocationResource = new FileSystemResource(metadataLocation);
        }
        if (!metadataLocationResource.exists() || !metadataLocationResource.isReadable()) {
            throw new FileNotFoundException("Resource does not exist or is unreadable");
        }
        return metadataLocationResource;
    }
}

package org.apereo.cas.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static org.springframework.util.ResourceUtils.*;

/**
 * Utility class to assist with resource operations.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class ResourceUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtils.class);

    // This constant covers both http and https
    private static final String HTTP_URL_PREFIX = "http";

    private ResourceUtils() {
    }

    /**
     * Gets resource from a String location.
     *
     * @param location the metadata location
     * @return the resource from
     * @throws IOException the exception
     */
    public static AbstractResource getRawResourceFrom(final String location) throws IOException {
        if (StringUtils.isBlank(location)) {
            throw new IllegalArgumentException("Provided location does not exist and is empty");
        }
        final AbstractResource res;
        if (location.toLowerCase().startsWith(HTTP_URL_PREFIX)) {
            res = new UrlResource(location);
        } else if (location.toLowerCase().startsWith(CLASSPATH_URL_PREFIX)) {
            res = new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()));
        } else {
            res = new FileSystemResource(StringUtils.remove(location, FILE_URL_PREFIX));
        }
        return res;
    }

    /**
     * Does resource exist?
     *
     * @param resource       the resource
     * @param resourceLoader the resource loader
     * @return the boolean
     */
    public static boolean doesResourceExist(final String resource, final ResourceLoader resourceLoader) {
        try {
            if (StringUtils.isNotBlank(resource)) {
                final Resource res = resourceLoader.getResource(resource);
                return doesResourceExist(res);
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Does resource exist?
     *
     * @param res the res
     * @return the boolean
     */
    public static boolean doesResourceExist(final Resource res) {
        if (res != null) {
            try {
                IOUtils.read(res.getInputStream(), new byte[1]);
                return res.contentLength() > 0;
            } catch (final Exception e) {
                LOGGER.trace(e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

    /**
     * Does resource exist?
     *
     * @param location the resource
     * @return the boolean
     */
    public static boolean doesResourceExist(final String location) {
        try {
            return getResourceFrom(location) != null;
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Gets resource from a String location.
     *
     * @param location the metadata location
     * @return the resource from
     * @throws IOException the exception
     */
    public static AbstractResource getResourceFrom(final String location) throws IOException {
        final AbstractResource metadataLocationResource = getRawResourceFrom(location);
        if (!metadataLocationResource.exists() || !metadataLocationResource.isReadable()) {
            throw new FileNotFoundException("Resource " + location + " does not exist or is unreadable");
        }
        return metadataLocationResource;
    }

    /**
     * Prepare classpath resource if needed file.
     *
     * @param resource the resource
     * @return the file
     */
    public static Resource prepareClasspathResourceIfNeeded(final Resource resource) {
        if (resource == null) {
            LOGGER.debug("No resource defined to prepare. Returning null");
            return null;
        }
        return prepareClasspathResourceIfNeeded(resource, false, resource.getFilename());
    }

    /**
     * If the provided resource is a classpath resource, running inside an embedded container,
     * and if the container is running in a non-exploded form, classpath resources become non-accessible.
     * So, this method will attempt to move resources out of classpath and onto a physical location
     * outside the context, typically in the "cas" directory of the temp system folder.
     *
     * @param resource     the resource
     * @param isDirectory  the if the resource is a directory, in which case entries will be copied over.
     * @param containsName the resource name pattern
     * @return the file
     */
    public static Resource prepareClasspathResourceIfNeeded(final Resource resource,
                                                            final boolean isDirectory,
                                                            final String containsName) {
        try {
            if (resource == null) {
                LOGGER.debug("No resource defined to prepare. Returning null");
                return null;
            }

            if (!ClassUtils.isAssignable(resource.getClass(), ClassPathResource.class)) {
                return resource;
            }
            if (org.springframework.util.ResourceUtils.isFileURL(resource.getURL())) {
                return resource;
            }

            final URL url = org.springframework.util.ResourceUtils.extractArchiveURL(resource.getURL());
            final File file = org.springframework.util.ResourceUtils.getFile(url);

            final File casDirectory = new File(FileUtils.getTempDirectory(), "cas");
            final File destination = new File(casDirectory, resource.getFilename());
            if (isDirectory) {
                FileUtils.forceMkdir(destination);
                FileUtils.cleanDirectory(destination);
            } else if (destination.exists()) {
                FileUtils.forceDelete(destination);
            }

            try (JarFile jFile = new JarFile(file)) {
                final Enumeration e = jFile.entries();
                while (e.hasMoreElements()) {
                    final ZipEntry entry = (ZipEntry) e.nextElement();
                    if (entry.getName().contains(resource.getFilename()) && entry.getName().contains(containsName)) {
                        try (InputStream stream = jFile.getInputStream(entry)) {
                            File copyDestination = destination;
                            if (isDirectory) {
                                final File entryFileName = new File(entry.getName());
                                copyDestination = new File(destination, entryFileName.getName());
                            }

                            try (FileWriter writer = new FileWriter(copyDestination)) {
                                IOUtils.copy(stream, writer, StandardCharsets.UTF_8);
                            }
                        }
                    }
                }
            }
            return new FileSystemResource(destination);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Is the resource a file?
     *
     * @param resource the resource
     * @return the boolean
     */
    public static boolean isFile(final String resource) {
        return StringUtils.isNotBlank(resource) && resource.startsWith(FILE_URL_PREFIX);
    }
}

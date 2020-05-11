package org.apereo.cas.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.jar.JarFile;

import static org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;
import static org.springframework.util.ResourceUtils.FILE_URL_PREFIX;

/**
 * Utility class to assist with resource operations.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@UtilityClass
public class ResourceUtils {
    /**
     * Empty resource.
     */
    public static final Resource EMPTY_RESOURCE = new ByteArrayResource(ArrayUtils.EMPTY_BYTE_ARRAY);

    private static final String HTTP_URL_PREFIX = "http";

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
        if (location.toLowerCase().startsWith(HTTP_URL_PREFIX)) {
            return new UrlResource(location);
        }
        if (location.toLowerCase().startsWith(CLASSPATH_URL_PREFIX)) {
            return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()));
        }
        return new FileSystemResource(StringUtils.remove(location, FILE_URL_PREFIX));
    }

    /**
     * Does resource exist?
     *
     * @param resource       the resource
     * @param resourceLoader the resource loader
     * @return true/false
     */
    public static boolean doesResourceExist(final String resource, final ResourceLoader resourceLoader) {
        try {
            if (StringUtils.isNotBlank(resource)) {
                val res = resourceLoader.getResource(resource);
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
     * @return true/false
     */
    public static boolean doesResourceExist(final Resource res) {
        if (res == null) {
            return false;
        }
        try {
            IOUtils.read(res.getInputStream(), new byte[1]);
            return res.contentLength() > 0;
        } catch (final FileNotFoundException e) {
            LOGGER.trace(e.getMessage());
            return false;
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Does resource exist?
     *
     * @param location the resource
     * @return true/false
     */
    public static boolean doesResourceExist(final String location) {
        try {
            getResourceFrom(location);
            return true;
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage());
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
        val resource = getRawResourceFrom(location);
        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException("Resource " + location + " does not exist or is unreadable");
        }
        return resource;
    }

    @SneakyThrows
    public static Resource exportClasspathResourceToFile(final File parentDirectory, final Resource resource) {
        LOGGER.trace("Preparing classpath resource [{}]", resource);
        if (resource == null) {
            LOGGER.warn("No resource defined to prepare. Returning null");
            return null;
        }
        if (!parentDirectory.exists() && !parentDirectory.mkdirs()) {
            LOGGER.warn("Unable to create folder [{}]", parentDirectory);
        }
        val destination = new File(parentDirectory, Objects.requireNonNull(resource.getFilename()));
        if (destination.exists()) {
            LOGGER.trace("Deleting resource directory [{}]", destination);
            FileUtils.forceDelete(destination);
        }
        try (val out = new FileOutputStream(destination)) {
            resource.getInputStream().transferTo(out);
        }
        return new FileSystemResource(destination);
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
    @SneakyThrows
    public static Resource prepareClasspathResourceIfNeeded(final Resource resource,
                                                            final boolean isDirectory,
                                                            final String containsName) {
        LOGGER.trace("Preparing possible classpath resource [{}]", resource);
        if (resource == null) {
            LOGGER.debug("No resource defined to prepare. Returning null");
            return null;
        }

        if (org.springframework.util.ResourceUtils.isFileURL(resource.getURL())) {
            return resource;
        }

        val url = org.springframework.util.ResourceUtils.extractArchiveURL(resource.getURL());
        val file = org.springframework.util.ResourceUtils.getFile(url);

        val destination = new File(FileUtils.getTempDirectory(), Objects.requireNonNull(resource.getFilename()));
        if (isDirectory) {
            LOGGER.trace("Creating resource directory [{}]", destination);
            FileUtils.forceMkdir(destination);
            FileUtils.cleanDirectory(destination);
        } else if (destination.exists()) {
            LOGGER.trace("Deleting resource directory [{}]", destination);
            FileUtils.forceDelete(destination);
        }

        LOGGER.trace("Processing file [{}]", file);
        try (val jFile = new JarFile(file)) {
            val e = jFile.entries();
            while (e.hasMoreElements()) {
                val entry = e.nextElement();
                val name = entry.getName();
                LOGGER.trace("Comparing [{}] against [{}] and pattern [{}]", name, resource.getFilename(), containsName);
                if (name.contains(resource.getFilename()) && RegexUtils.find(containsName, name)) {
                    try (val stream = jFile.getInputStream(entry)) {
                        var copyDestination = destination;
                        if (isDirectory) {
                            val entryFileName = new File(name);
                            copyDestination = new File(destination, entryFileName.getName());
                        }
                        LOGGER.trace("Copying resource entry [{}] to [{}]", name, copyDestination);
                        try (val writer = Files.newBufferedWriter(copyDestination.toPath(), StandardCharsets.UTF_8)) {
                            IOUtils.copy(stream, writer, StandardCharsets.UTF_8);
                        }
                    }
                }
            }
        }
        return new FileSystemResource(destination);
    }


    /**
     * Build input stream resource from string value.
     *
     * @param value       the value
     * @param description the description
     * @return the input stream resource
     */
    public static InputStreamResource buildInputStreamResourceFrom(final String value, final String description) {
        val reader = new StringReader(value);
        val is = new ReaderInputStream(reader, StandardCharsets.UTF_8);
        return new InputStreamResource(is, description);
    }

    /**
     * Is the resource a file?
     *
     * @param resource the resource
     * @return true/false
     */
    public static boolean isFile(final String resource) {
        return StringUtils.isNotBlank(resource) && resource.startsWith(FILE_URL_PREFIX);
    }

    /**
     * Is file boolean.
     *
     * @param resource the resource
     * @return true/false
     */
    public static boolean isFile(final Resource resource) {
        try {
            resource.getFile();
            return true;
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage());
        }
        return false;
    }

    /**
     * Is jar resource ?.
     *
     * @param resource the resource
     * @return true/false
     */
    public static boolean isJarResource(final Resource resource) {
        try {
            return "jar".equals(resource.getURI().getScheme());
        } catch (final IOException e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return false;
    }
}

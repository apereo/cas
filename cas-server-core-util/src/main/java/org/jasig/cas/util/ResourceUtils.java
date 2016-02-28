package org.jasig.cas.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Utility class to assist with resource operations.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public final class ResourceUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtils.class);
    
    private ResourceUtils() {
    }

    /**
     * Gets resource from a String location.
     *
     * @param metadataLocation the metadata location
     * @return the resource from
     * @throws IOException the exception
     */
    public static AbstractResource getRawResourceFrom(final String metadataLocation) throws IOException {
        final AbstractResource metadataLocationResource;
        if (metadataLocation.toLowerCase().startsWith("http")) {
            metadataLocationResource = new UrlResource(metadataLocation);
        } else if (metadataLocation.toLowerCase().startsWith("classpath")) {
            metadataLocationResource = new ClassPathResource(metadataLocation);
        } else {
            metadataLocationResource = new FileSystemResource(metadataLocation);
        }
        return metadataLocationResource;
    }
    
    /**
     * Gets resource from a String location.
     *
     * @param metadataLocation the metadata location
     * @return the resource from
     * @throws IOException the exception
     */
    public static AbstractResource getResourceFrom(final String metadataLocation) throws IOException {
        final AbstractResource metadataLocationResource = getRawResourceFrom(metadataLocation);
        if (!metadataLocationResource.exists() || !metadataLocationResource.isReadable()) {
            throw new FileNotFoundException("Resource does not exist or is unreadable");
        }
        return metadataLocationResource;
    }

    /**
     * If the provided resource is a classpath resource, running inside an embedded container,
     * and if the container is running in a non-exploded form, classpath resources become non-accessible. 
     * So, this method will attempt to move resources out of classpath and onto a physical location
     * outside the context, typically in the "cas" directory of the temp system folder.
     *
     * @param resource            the resource
     * @param isDirectory         the if the resource is a directory, in which case entries will be copied over.
     * @param containsName the resource name pattern
     * @return the file
     */
    public static File prepareClasspathResourceIfNeeded(final Resource resource,
                                                        final boolean isDirectory,
                                                        final String containsName) {
        try {

            if (!ClassUtils.isAssignable(resource.getClass(), ClassPathResource.class)) {
                return resource.getFile();
            }
            if (org.springframework.util.ResourceUtils.isFileURL(resource.getURL())) {
                return resource.getFile();
            }
            
            final URL url = org.springframework.util.ResourceUtils.extractArchiveURL(resource.getURL());
            final File file = org.springframework.util.ResourceUtils.getFile(url);

            final File casDirectory = new File(FileUtils.getTempDirectory(), "cas");
            final File destination = new File(casDirectory, resource.getFilename());
            if (isDirectory) {
                FileUtils.forceMkdir(destination);
                FileUtils.cleanDirectory(destination);
            }

            final JarFile jFile = new JarFile(file);
            final Enumeration e = jFile.entries();
            while (e.hasMoreElements()) {
                final ZipEntry entry = (ZipEntry) e.nextElement();
                if (entry.getName().contains(resource.getFilename()) && entry.getName().contains(containsName)) {
                    try (final InputStream stream = jFile.getInputStream(entry)) {
                        File copyDestination = destination;
                        if (isDirectory) {
                            final File entryFileName = new File(entry.getName());
                            copyDestination = new File(destination, entryFileName.getName());
                        }
                        
                        try (final FileWriter writer = new FileWriter(copyDestination)) {
                            IOUtils.copy(stream, writer);
                        }
                    }
                }
            }
            return destination;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package org.apereo.cas.util.io;


import org.apache.commons.io.IOUtils;
import org.apereo.cas.util.ResourceUtils;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link WatchableResource}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class WatchableResource extends AbstractResource implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(WatchableResource.class);
    private static final int DEFAULT_INTERVAL = 5_000;
    
    private Resource resource;
    private Thread thread;

    public WatchableResource(final Resource resource) {
        this(resource, DEFAULT_INTERVAL);
    }
    
    public WatchableResource(final Resource resource, final int interval) {
        this.resource = resource;
        if (getFile() != null) {
            try {
                this.thread = new Thread(new PathWatcher(getFile().toPath(),
                        file -> {
                        },
                        Unchecked.consumer(file -> {
                            LOGGER.debug("Detected modification at [{}]. Reloading...", file.getCanonicalPath());
                            this.resource = ResourceUtils.getRawResourceFrom(file.getCanonicalPath());
                        }),
                        file -> {
                        },
                        interval));
                this.thread.start();
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public File getFile() {
        try {
            return this.resource.getFile();
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public URL getURL() {
        try {
            if (getFile() != null) {
                return getURI().toURL();
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String getFilename() {
        try {
            if (getFile() != null) {
                return getFile().getName();
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public URI getURI() {
        try {
            if (getFile() != null) {
                return getFile().toURI();
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public String getContent() {
        try {
            if (getFile() != null) {
                return IOUtils.toString(getInputStream(), StandardCharsets.UTF_8);
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean isReadable() {
        return this.resource.isReadable();
    }

    @Override
    public boolean isOpen() {
        return this.resource.isOpen();
    }

    @Override
    public String getDescription() {
        return this.resource.getDescription();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.resource.getInputStream();
    }

    @Override
    public void close() {
        if (this.thread != null) {
            this.thread.interrupt();
        }
    }
}

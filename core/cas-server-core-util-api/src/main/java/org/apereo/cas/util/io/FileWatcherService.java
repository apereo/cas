package org.apereo.cas.util.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.function.Consumer;

/**
 * This is {@link FileWatcherService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class FileWatcherService extends PathWatcherService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcherService.class);
    
    public FileWatcherService(final File watchableFile, final Consumer<File> onModify) {
        super(watchableFile.getParentFile(), file -> {
            if (file.getPath().equals(watchableFile.getPath())) {
                LOGGER.debug("Detected change in file [{}] and calling change consumer to handle event", file);
                onModify.accept(file);
            }
        });
    }
}

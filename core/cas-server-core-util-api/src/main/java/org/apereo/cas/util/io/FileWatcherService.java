package org.apereo.cas.util.io;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.function.Consumer;

/**
 * This is {@link FileWatcherService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class FileWatcherService extends PathWatcherService {

    
    public FileWatcherService(final File watchableFile, final Consumer<File> onModify) {
        super(watchableFile.getParentFile(), file -> {
            if (file.getPath().equals(watchableFile.getPath())) {
                LOGGER.debug("Detected change in file [{}] and calling change consumer to handle event", file);
                onModify.accept(file);
            }
        });
    }
}

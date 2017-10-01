package org.apereo.cas.util.io;

import java.io.File;
import java.util.function.Consumer;

/**
 * This is {@link FileWatcherService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class FileWatcherService extends PathWatcherService {
    public FileWatcherService(final File watchableFile, final Consumer<File> onModify) {
        super(watchableFile.getParentFile(), file -> {
            if (file.getPath().equals(watchableFile.getPath())) {
                onModify.accept(file);
            }
        });
    }
}

package org.apereo.cas.util.io;

import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import java.io.File;

/**
 * This is {@link FileWatcherService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class FileWatcherService extends PathWatcherService {

    public FileWatcherService(final File watchableFile, final CheckedConsumer<File> onCreate,
                              final CheckedConsumer<File> onModify, final CheckedConsumer<File> onDelete) {
        super(watchableFile.getParentFile().toPath(),
            getWatchedFileConsumer(watchableFile, onCreate),
            getWatchedFileConsumer(watchableFile, onModify),
            getWatchedFileConsumer(watchableFile, onDelete));
    }

    public FileWatcherService(final File watchableFile, final CheckedConsumer<File> onModify) {
        super(watchableFile.getParentFile(), getWatchedFileConsumer(watchableFile, onModify));
    }

    private static CheckedConsumer<File> getWatchedFileConsumer(
        final File watchableFile, final CheckedConsumer<File> consumer) {
        return file -> {
            if (file.getPath().equals(watchableFile.getPath())) {
                LOGGER.trace("Detected change in file [{}] and calling change consumer to handle event", file);
                CheckedConsumer.sneaky(consumer).accept(file);
            }
        };
    }
}

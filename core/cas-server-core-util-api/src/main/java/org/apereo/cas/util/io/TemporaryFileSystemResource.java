package org.apereo.cas.util.io;

import module java.base;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.FileSystemResource;

/**
 * This is {@link TemporaryFileSystemResource}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class TemporaryFileSystemResource extends FileSystemResource {

    public TemporaryFileSystemResource(final File file) {
        super(file);
    }

    @NonNull
    @Override
    public InputStream getInputStream() throws IOException {
        return new FilterInputStream(super.getInputStream()) {

            @Override
            public void close() throws IOException {
                closeThenDeleteFile(this.in);
            }

        };
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @NonNull
    @Override
    public ReadableByteChannel readableChannel() throws IOException {
        val readableChannel = super.readableChannel();
        return new ReadableByteChannel() {

            @Override
            public boolean isOpen() {
                return readableChannel.isOpen();
            }

            @Override
            public void close() throws IOException {
                closeThenDeleteFile(readableChannel);
            }

            @Override
            public int read(final ByteBuffer dst) throws IOException {
                return readableChannel.read(dst);
            }
        };
    }

    private void closeThenDeleteFile(final Closeable closeable) throws IOException {
        try {
            closeable.close();
        } finally {
            deleteFile();
        }
    }

    private void deleteFile() {
        try {
            Files.delete(getFile().toPath());
        } catch (final IOException ex) {
            val msg = String.format("Failed to delete temporary heap dump file %s", getFile());
            LOGGER.warn(msg, ex);
        }
    }

}

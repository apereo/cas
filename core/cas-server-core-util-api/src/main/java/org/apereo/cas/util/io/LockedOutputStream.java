package org.apereo.cas.util.io;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

/**
 * Buffered output stream around a file that is exclusively locked for the
 * lifetime of the stream.
 * @author Misagh Moayyed
 * @author Marvin S. Addison
 * @since 4.1.0
 */
public class LockedOutputStream extends BufferedOutputStream {

    /** Lock held on file underneath stream. */
    private final FileLock lock;

    /** Flag to indicate underlying stream is already closed. */
    private boolean closed;

    /**
     * Creates a new instance by obtaining a lock on the underlying stream
     * that is held until the stream is closed.
     *
     * @param out Output stream.
     * @throws IOException If a lock cannot be obtained on the file.
     */
    public LockedOutputStream(final FileOutputStream out) throws IOException {
        super(out);
        this.lock = out.getChannel().lock();
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        try {
            this.lock.release();
        } finally {
            this.closed = true;
            super.close();
        }
    }
}

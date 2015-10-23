/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.util;

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
public final class LockedOutputStream extends BufferedOutputStream {

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        try {
            lock.release();
        } finally {
            closed = true;
            super.close();
        }
    }
}

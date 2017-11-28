package org.apereo.cas.util.io;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link CopyServletOutputStream}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CopyServletOutputStream extends ServletOutputStream {
    private static final int BUFFER_SIZE = 1024;
    
    private final OutputStream outputStream;
    private final ByteArrayOutputStream copy;

    public CopyServletOutputStream(final OutputStream outputStream) {
        this.outputStream = outputStream;
        this.copy = new ByteArrayOutputStream(BUFFER_SIZE);
    }

    @Override
    public void write(final int b) throws IOException {
        outputStream.write(b);
        copy.write(b);
    }

    public byte[] getCopy() {
        return copy.toByteArray();
    }

    public String getStringCopy() {
        return new String(getCopy(), StandardCharsets.UTF_8);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(final WriteListener writeListener) {
    }
}

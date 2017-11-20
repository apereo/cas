package org.apereo.cas.util.io;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This is {@link CopyPrintWriter}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CopyPrintWriter extends PrintWriter {

    private StringBuilder copy = new StringBuilder();

    public CopyPrintWriter() {
        super(new StringWriter());
    }

    @Override
    public void write(final int c) {
        copy.append((char) c);
        super.write(c);
    }

    @Override
    public void write(final char[] chars, final int offset, final int length) {
        copy.append(chars, offset, length);
        super.write(chars, offset, length);
    }

    @Override
    public void write(final String string, final int offset, final int length) {
        copy.append(string, offset, length);
        super.write(string, offset, length);
    }

    public String getCopy() {
        return copy.toString();
    }

}

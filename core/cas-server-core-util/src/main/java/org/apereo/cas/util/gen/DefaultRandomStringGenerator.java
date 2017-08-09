package org.apereo.cas.util.gen;

import java.util.stream.IntStream;

/**
 * Implementation of the RandomStringGenerator that allows you to define the
 * length of the random part.
 *
 * @author Scott Battaglia

 * @since 3.0.0
 */
public class DefaultRandomStringGenerator extends AbstractRandomStringGenerator {

    /** The array of printable characters to be used in our random string. */
    private static final char[] PRINTABLE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345679".toCharArray();

    public DefaultRandomStringGenerator() {
        super();
    }

    public DefaultRandomStringGenerator(final int defaultLength) {
        super(defaultLength);
    }

    /**
     * Convert bytes to string, taking into account {@link #PRINTABLE_CHARACTERS}.
     *
     * @param random the random
     * @return the string
     */
    protected String convertBytesToString(final byte[] random) {
        final char[] output = new char[random.length];
        IntStream.range(0, random.length).forEach(i -> {
            final int index = Math.abs(random[i] % PRINTABLE_CHARACTERS.length);
            output[i] = PRINTABLE_CHARACTERS[index];
        });

        return new String(output);
    }
}

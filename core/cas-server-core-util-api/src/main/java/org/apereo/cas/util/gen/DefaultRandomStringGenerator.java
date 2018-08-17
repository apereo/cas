package org.apereo.cas.util.gen;

import lombok.NoArgsConstructor;
import lombok.val;

import java.util.stream.IntStream;

/**
 * Implementation of the RandomStringGenerator that allows you to define the
 * length of the random part.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@NoArgsConstructor
public class DefaultRandomStringGenerator extends AbstractRandomStringGenerator {

    /**
     * The array of printable characters to be used in our random string.
     */
    private static final char[] PRINTABLE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345679".toCharArray();

    public DefaultRandomStringGenerator(final int defaultLength) {
        super(defaultLength);
    }

    /**
     * Convert bytes to string, taking into account {@link #PRINTABLE_CHARACTERS}.
     *
     * @param random the random
     * @return the string
     */
    @Override
    protected String convertBytesToString(final byte[] random) {
        val output = new char[random.length];
        IntStream.range(0, random.length).forEach(i -> {
            val printableCharacters = getPrintableCharacters();
            val index = Math.abs(random[i] % printableCharacters.length);
            output[i] = printableCharacters[index];
        });
        return new String(output);
    }

    /**
     * Get printable characters char [].
     *
     * @return the char []
     */
    protected char[] getPrintableCharacters() {
        return PRINTABLE_CHARACTERS;
    }
}

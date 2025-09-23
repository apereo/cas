package org.apereo.cas.util.gen;

/**
 * Implementation of the RandomStringGenerator that allows you to define the
 * length of the random part.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class DefaultRandomNumberGenerator extends DefaultRandomStringGenerator {
    private static final char[] PRINTABLE_CHARACTERS = "0123456789".toCharArray();

    public DefaultRandomNumberGenerator(final int defaultLength) {
        super(defaultLength);
    }

    @Override
    protected char[] getPrintableCharacters() {
        return PRINTABLE_CHARACTERS;
    }
}

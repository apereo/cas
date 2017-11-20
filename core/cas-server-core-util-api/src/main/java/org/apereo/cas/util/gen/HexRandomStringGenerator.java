package org.apereo.cas.util.gen;

import org.apereo.cas.util.EncodingUtils;

/**
 * This is {@link HexRandomStringGenerator}.
 * Hex encoding implementation of the RandomStringGenerator that allows you to define the
 * length of the random part.
 *
 * @author Timur Duehr

 * @since 5.2.0
 */
public class HexRandomStringGenerator extends AbstractRandomStringGenerator {

    public HexRandomStringGenerator() {
        super();
    }

    public HexRandomStringGenerator(final int defaultLength) {
        super(defaultLength);
    }

    @Override
    protected String convertBytesToString(final byte[] random) {
        return EncodingUtils.hexEncode(random);
    }
}

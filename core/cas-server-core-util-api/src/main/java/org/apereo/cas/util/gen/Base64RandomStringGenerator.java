package org.apereo.cas.util.gen;

import org.apereo.cas.util.EncodingUtils;

import lombok.NoArgsConstructor;

/**
 * This is {@link Base64RandomStringGenerator}.
 * <p>
 * URL safe base64 encoding implementation of the RandomStringGenerator that allows you to define the
 * length of the random part.
 *
 * @author Timur Duehr
 * @since 5.2.0
 */
@NoArgsConstructor
public class Base64RandomStringGenerator extends AbstractRandomStringGenerator {

    public Base64RandomStringGenerator(final long defaultLength) {
        super(defaultLength);
    }

    /**
     * Converts byte[] to String by Base64 encoding.
     *
     * @param random raw bytes
     * @return a converted String
     */
    @Override
    protected String convertBytesToString(final byte[] random) {
        return EncodingUtils.encodeUrlSafeBase64(random);
    }

}

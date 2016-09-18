package org.apereo.cas.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * This is {@link CompressionUtils}
 * that encapsulates common compression calls and operations
 * in one spot.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
public final class CompressionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionUtils.class);

    private static final int INFLATED_ARRAY_LENGTH = 10000;
    
    /**
     * Private ctor for a utility class.
     */
    private CompressionUtils() {
    }

    /**
     * Inflate the given byte array by {@link #INFLATED_ARRAY_LENGTH}.
     *
     * @param bytes the bytes
     * @return the array as a string with {@code UTF-8} encoding
     */
    public static String inflate(final byte[] bytes) {
        final Inflater inflater = new Inflater(true);
        final byte[] xmlMessageBytes = new byte[INFLATED_ARRAY_LENGTH];

        final byte[] extendedBytes = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, extendedBytes, 0, bytes.length);
        extendedBytes[bytes.length] = 0;

        inflater.setInput(extendedBytes);

        try {
            final int resultLength = inflater.inflate(xmlMessageBytes);
            inflater.end();

            if (!inflater.finished()) {
                throw new RuntimeException("buffer not large enough.");
            }

            inflater.end();
            return new String(xmlMessageBytes, 0, resultLength, StandardCharsets.UTF_8);
        } catch (final DataFormatException e) {
            return null;
        }
    }


    /**
     * Deflate the given bytes using zlib.
     *
     * @param bytes the bytes
     * @return the converted string
     */
    public static String deflate(final byte[] bytes) {
        final String data = new String(bytes, StandardCharsets.UTF_8);
        return deflate(data);
    }

    /**
     * Deflate the given string via a {@link java.util.zip.Deflater}.
     *
     * @param data the data
     * @return base64 encoded string
     */
    public static String deflate(final String data) {
        final Deflater deflater = new Deflater();
        deflater.setInput(data.getBytes(StandardCharsets.UTF_8));
        deflater.finish();
        final byte[] buffer = new byte[data.length()];
        final int resultSize = deflater.deflate(buffer);
        final byte[] output = new byte[resultSize];
        System.arraycopy(buffer, 0, output, 0, resultSize);
        return EncodingUtils.encodeBase64(output);
    }

    /**
     * Decode the byte[] in base64 to a string.
     *
     * @param bytes the data to encode
     * @return the new string
     */
    public static String decodeByteArrayToString(final byte[] bytes) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] buf = new byte[bytes.length];
        try (InflaterInputStream iis = new InflaterInputStream(bais)) {
            int count = iis.read(buf);
            while (count != -1) {
                baos.write(buf, 0, count);
                count = iis.read(buf);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            LOGGER.error("Base64 decoding failed", e);
            return null;
        }
    }

}

package org.jasig.cas.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.jasig.cas.CipherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * This is {@link CompressionUtils}
 * that encapsulates common base64 calls and operations
 * in one spot.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
public final class CompressionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionUtils.class);

    private static final int INFLATED_ARRAY_LENGTH = 10000;

    private static final String UTF8_ENCODING = "UTF-8";

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
            return new String(xmlMessageBytes, 0, resultLength, "UTF-8");
        } catch (final DataFormatException e) {
            return null;
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot find encoding: UTF-8", e);
        }
    }


    /**
     * Deflate the given bytes using zlib.
     * The result will be base64 encoded with {@link #UTF8_ENCODING}.
     *
     * @param bytes the bytes
     * @return the converted string
     */
    public static String deflate(final byte[] bytes) {
        final String data = new String(bytes, Charset.forName(UTF8_ENCODING));
        return deflate(data);
    }

    /**
     * Deflate the given string via a {@link java.util.zip.Deflater}.
     * The result will be base64 encoded with {@link #UTF8_ENCODING}.
     *
     * @param data the data
     * @return base64 encoded string
     */
    public static String deflate(final String data) {
        try {
            final Deflater deflater = new Deflater();
            deflater.setInput(data.getBytes(UTF8_ENCODING));
            deflater.finish();
            final byte[] buffer = new byte[data.length()];
            final int resultSize = deflater.deflate(buffer);
            final byte[] output = new byte[resultSize];
            System.arraycopy(buffer, 0, output, 0, resultSize);
            return encodeBase64(output);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot find encoding:" + UTF8_ENCODING, e);
        }
    }

    /**
     * Base64-encode the given byte[] as a string.
     *
     * @param data the byte array to encode
     * @return the encoded string
     */
    public static String encodeBase64(final byte[] data) {
        return Base64.encodeBase64String(data);
    }

    /**
     * Base64-decode the given string as byte[].
     *
     * @param data the base64 string
     * @return the encoded array
     */
    public static byte[] decodeBase64(final String data) {
        return Base64.decodeBase64(data);
    }

    /**
     * Base64-encode the given byte[] as a byte[].
     *
     * @param data the byte array to encode
     * @return the byte[] in base64
     */
    public static byte[] encodeBase64ToByteArray(final byte[] data) {
        return Base64.encodeBase64(data);
    }

    /**
     * Base64 decode operation, which retrieves the equivalent
     * byte[] of the data in {@code UTF-8} encoding
     * and decodes the result.
     *
     * @param data the data to encode
     * @return the base64 decoded byte[] or null
     */
    public static byte[] decodeBase64ToByteArray(final String data) {
        try {
            final byte[] bytes = data.getBytes(UTF8_ENCODING);
            return decodeBase64ToByteArray(bytes);
        } catch (final Exception e) {
            LOGGER.error("Base64 decoding failed", e);
            return null;
        }
    }

    /**
     * Decode the byte[] in base64.
     *
     * @param data the data to encode
     * @return the base64 decoded byte[] or null
     */
    public static byte[] decodeBase64ToByteArray(final byte[] data) {
        try {
            return Base64.decodeBase64(data);
        } catch (final Exception e) {
            LOGGER.error("Base64 decoding failed", e);
            return null;
        }
    }

    /**
     * Decode the byte[] in base64 to a string.
     *
     * @param bytes the data to encode
     * @return the new string in {@link #UTF8_ENCODING}.
     */
    public static String decodeByteArrayToString(final byte[] bytes) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] buf = new byte[bytes.length];
        try (final InflaterInputStream iis = new InflaterInputStream(bais)) {
            int count = iis.read(buf);
            while (count != -1) {
                baos.write(buf, 0, count);
                count = iis.read(buf);
            }
            return new String(baos.toByteArray(), Charset.forName(UTF8_ENCODING));
        } catch (final Exception e) {
            LOGGER.error("Base64 decoding failed", e);
            return null;
        }
    }

    /**
     * Serialize and encode object.
     *
     * @param cipher the cipher
     * @param object the object
     * @return the byte []
     * @since 4.2
     */
    public static byte[] serializeAndEncodeObject(final CipherExecutor<byte[], byte[]> cipher,
                                                  final Serializable object) {
        final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        SerializationUtils.serialize(object, outBytes);
        return cipher.encode(outBytes.toByteArray());
    }

    /**
     * Decode and serialize object.
     *
     * @param <T>  the type parameter
     * @param object the object
     * @param cipher the cipher
     * @param type the type
     * @return the t
     * @since 4.2
     */
    public static <T> T decodeAndSerializeObject(final byte[] object,
                                                 final CipherExecutor<byte[], byte[]> cipher,
                                                 final Class<? extends Serializable> type) {
        final byte[] decoded = cipher.decode(object);
        final Object result = SerializationUtils.deserialize(decoded);
        if (!type.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Decoded object is of type " + result.getClass()
                    + " when we were expecting " + type);
        }
        return (T) result;
    }

    /**
     * Calculates the SHA-512 digest and returns the value as a hex string.
     *
     * @param input the input
     * @return the value as hex
     * @since 4.2
     */
    public static String sha512Hex(final String input) {
        return DigestUtils.sha512Hex(input);
    }
}

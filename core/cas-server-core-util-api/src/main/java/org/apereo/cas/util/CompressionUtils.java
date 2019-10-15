package org.apereo.cas.util;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * This is {@link CompressionUtils}
 * that encapsulates common compression calls and operations
 * in one spot.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@UtilityClass
public class CompressionUtils {
    private static final int INFLATED_ARRAY_LENGTH = 10000;

    /**
     * Deflate the given bytes using zlib.
     *
     * @param bytes the bytes
     * @return the converted string
     */
    public static String deflate(final byte[] bytes) {
        val data = new String(bytes, StandardCharsets.UTF_8);
        return deflate(data);
    }

    /**
     * Deflate the given string via a {@link java.util.zip.Deflater}.
     *
     * @param data the data
     * @return base64 encoded string
     */
    public static String deflate(final String data) {
        val deflater = new Deflater();
        deflater.setInput(data.getBytes(StandardCharsets.UTF_8));
        deflater.finish();
        val buffer = new byte[data.length()];
        val resultSize = deflater.deflate(buffer);
        deflater.end();
        val output = new byte[resultSize];
        System.arraycopy(buffer, 0, output, 0, resultSize);
        return EncodingUtils.encodeBase64(output);
    }

    /**
     * Inflate the byte[] to a string.
     *
     * @param bytes the data to decode
     * @return the new string
     */
    public static String inflate(final byte[] bytes) {
        val inflater = new Inflater(true);
        val xmlMessageBytes = new byte[INFLATED_ARRAY_LENGTH];

        val extendedBytes = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, extendedBytes, 0, bytes.length);
        extendedBytes[bytes.length] = 0;
        inflater.setInput(extendedBytes);

        try {
            val resultLength = inflater.inflate(xmlMessageBytes);
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
     * First decode base64 String to byte array, then use ZipInputStream to revert the byte array to a
     * string.
     *
     * @param zippedBase64Str the zipped base 64 str
     * @return the string, or null
     */
    @SneakyThrows
    public static String decompress(final String zippedBase64Str) {
        val bytes = EncodingUtils.decodeBase64(zippedBase64Str);
        try (val zi = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            return IOUtils.toString(zi, Charset.defaultCharset());
        }
    }

    /**
     * Decode the byte[] in base64 to a string.
     *
     * @param bytes the data to encode
     * @return the new string
     */
    @SneakyThrows
    public static String decodeByteArrayToString(final byte[] bytes) {
        @Cleanup
        val bais = new ByteArrayInputStream(bytes);
        @Cleanup
        val baos = new ByteArrayOutputStream();
        val buf = new byte[bytes.length];
        try (val iis = new InflaterInputStream(bais)) {
            var count = iis.read(buf);
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


    /**
     * Use ZipOutputStream to zip text to byte array, then convert
     * byte array to base64 string, so it can be transferred via http request.
     *
     * @param srcTxt the src txt
     * @return the string in UTF-8 format and base64'ed, or null.
     */
    @SneakyThrows
    public static String compress(final String srcTxt) {
        try (val rstBao = new ByteArrayOutputStream(); val zos = new GZIPOutputStream(rstBao)) {
            zos.write(srcTxt.getBytes(StandardCharsets.UTF_8));
            zos.flush();
            zos.finish();
            val bytes = rstBao.toByteArray();
            val base64 = StringUtils.remove(EncodingUtils.encodeBase64(bytes), '\0');
            return new String(StandardCharsets.UTF_8.encode(base64).array(), StandardCharsets.UTF_8);
        }
    }
}

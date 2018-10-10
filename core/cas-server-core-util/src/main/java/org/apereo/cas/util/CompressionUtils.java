package org.apereo.cas.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
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

    /**
     * Private ctor for a utility class.
     */
    private CompressionUtils() {
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
     * Inflate the byte[] to a string.
     *
     * @param bytes the data to decode
     * @return the new string
     */
    public static String inflate(final byte[] bytes) {
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
    public static String decompress(final String zippedBase64Str) {
        GZIPInputStream zi = null;
        try {
            final byte[] bytes = EncodingUtils.decodeBase64(zippedBase64Str);
            zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
            return IOUtils.toString(zi, Charset.defaultCharset());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(zi);
        }
        return null;
    }

    /**
     * Use ZipOutputStream to zip text to byte array, then convert
     * byte array to base64 string, so it can be transferred via http request.
     *
     * @param srcTxt the src txt
     * @return the string in UTF-8 format and base64'ed, or null.
     */
    public static String compress(final String srcTxt) {
        try {
            final ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
            final GZIPOutputStream zos = new GZIPOutputStream(rstBao);
            zos.write(srcTxt.getBytes(StandardCharsets.UTF_8));
            IOUtils.closeQuietly(zos);
            final byte[] bytes = rstBao.toByteArray();
            final String base64 = StringUtils.remove(EncodingUtils.encodeBase64(bytes), '\0');
            return new String(StandardCharsets.UTF_8.encode(base64).array(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}

/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.InflaterOutputStream;

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
     * @return the array as a string with <code>UTF-8</code> encoding
     */
    public static String inflate(final byte[] bytes) {
        try (ByteArrayInputStream inb = new ByteArrayInputStream(bytes);
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             InflaterOutputStream ios = new InflaterOutputStream(out);) {
            IOUtils.copy(inb, ios);
            return new String(out.toByteArray(), UTF8_ENCODING);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
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
     * byte[] of the data in <code>UTF-8</code> encoding
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


}

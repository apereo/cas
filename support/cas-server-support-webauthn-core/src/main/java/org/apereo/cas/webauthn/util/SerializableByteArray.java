package org.apereo.cas.webauthn.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yubico.internal.util.BinaryUtil;
import com.yubico.internal.util.json.JsonStringSerializable;
import com.yubico.internal.util.json.JsonStringSerializer;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.data.exception.HexException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bouncycastle.util.Arrays;

import java.util.Base64;

/**
 * This is {@link SerializableByteArray}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonSerialize(using = JsonStringSerializer.class)
@EqualsAndHashCode
@ToString(of = "base64", includeFieldNames = false)
public class SerializableByteArray implements Comparable<SerializableByteArray>, JsonStringSerializable {

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    private static final Base64.Encoder BASE64URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64URL_DECODER = Base64.getUrlDecoder();

    private final byte[] bytes;
    private final String base64;
    
    public SerializableByteArray(final byte[] bytes) {
        this.bytes = BinaryUtil.copy(bytes);
        this.base64 = BASE64URL_ENCODER.encodeToString(this.bytes);
    }

    @JsonCreator
    private SerializableByteArray(final String base64) throws Base64UrlException {
        try {
            this.bytes = BASE64URL_DECODER.decode(base64);
        } catch (final IllegalArgumentException e) {
            throw new Base64UrlException("Invalid Base64Url encoding: " + base64, e);
        }
        this.base64 = base64;
    }

    public static SerializableByteArray fromBase64(final String base64) {
        return new SerializableByteArray(BASE64_DECODER.decode(base64));
    }

    public static SerializableByteArray fromBase64Url(final String base64) throws Base64UrlException {
        return new SerializableByteArray(base64);
    }

    public static SerializableByteArray fromHex(final String hex) throws HexException {
        try {
            return new SerializableByteArray(BinaryUtil.fromHex(hex));
        } catch (final Exception e) {
            throw new HexException("Invalid hexadecimal encoding: " + hex, e);
        }
    }

    public SerializableByteArray concat(final SerializableByteArray tail) {
        return new SerializableByteArray(Arrays.concatenate(this.bytes, tail.bytes));
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return this.bytes.length;
    }

    public byte[] getBytes() {
        return BinaryUtil.copy(bytes);
    }

    public String getBase64() {
        return BASE64_ENCODER.encodeToString(bytes);
    }

    public String getBase64Url() {
        return toJsonString();
    }

    public String getHex() {
        return BinaryUtil.toHex(bytes);
    }
    
    @Override
    public String toJsonString() {
        return base64;
    }

    @Override
    public int compareTo(final SerializableByteArray other) {
        if (bytes.length != other.bytes.length) {
            return bytes.length - other.bytes.length;
        }
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] != other.bytes[i]) {
                return bytes[i] - other.bytes[i];
            }
        }
        return 0;
    }
}

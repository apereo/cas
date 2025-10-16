package com.yubico.webauthn.attestation.matcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.yubico.webauthn.attestation.DeviceMatcher;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.exception.HexException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

/**
 * This is {@link ExtensionMatcher}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class ExtensionMatcher implements DeviceMatcher {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static final String SELECTOR_TYPE = "x509Extension";

    private static final String EXTENSION_KEY = "key";
    private static final String EXTENSION_VALUE = "value";
    private static final String EXTENSION_VALUE_TYPE = "type";
    private static final String EXTENSION_VALUE_VALUE = "value";
    private static final String EXTENSION_VALUE_TYPE_HEX = "hex";

    @Override
    public boolean matches(final X509Certificate attestationCertificate, final JsonNode parameters) {
        val matchKey = parameters.get(EXTENSION_KEY).asText();
        val matchValue = parameters.get(EXTENSION_VALUE);
        val extensionValue = attestationCertificate.getExtensionValue(matchKey);
        if (extensionValue != null) {
            if (matchValue == null) {
                return true;
            } else {
                try {
                    val value = ASN1Primitive.fromByteArray(extensionValue);

                    if (matchValue.isObject()) {
                        if (matchTypedValue(matchKey, matchValue, value)) {
                            return true;
                        }
                    } else if (matchValue.isTextual()) {
                        if (matchStringValue(matchKey, matchValue, value)) return true;
                    }
                } catch (final IOException e) {
                    LOGGER.error(
                        "Failed to parse extension value as ASN1: {}",
                        new ByteArray(extensionValue).getHex(),
                        e);
                }
            }
        }
        return false;
    }

    private static boolean matchStringValue(final String matchKey, final JsonNode matchValue, final ASN1Primitive value) {
        if (value instanceof final DEROctetString octetString) {
            val readValue = new String(octetString.getOctets(), CHARSET);
            return matchValue.asText().equals(readValue);
        }
        LOGGER.debug("Expected text string value for extension {}, was: {}", matchKey, value);
        return false;
    }

    private static boolean matchTypedValue(final String matchKey, final JsonNode matchValue, final ASN1Primitive value) {
        val extensionValueType = matchValue.get(EXTENSION_VALUE_TYPE).textValue();
        return switch (extensionValueType) {
            case EXTENSION_VALUE_TYPE_HEX -> matchHex(matchKey, matchValue, value);
            default -> throw new IllegalArgumentException(
                String.format(
                    "Unknown extension value type \"%s\" for extension \"%s\"",
                    extensionValueType, matchKey));
        };
    }

    private static boolean matchHex(final String matchKey, final JsonNode matchValue, final ASN1Primitive value) {
        val matchValueString = matchValue.get(EXTENSION_VALUE_VALUE).textValue();
        final ByteArray matchBytes;
        try {
            matchBytes = ByteArray.fromHex(matchValueString);
        } catch (final HexException e) {
            throw new IllegalArgumentException(
                String.format("Bad hex value in extension %s: %s", matchKey, matchValueString));
        }

        final ASN1Primitive innerValue;
        if (value instanceof final DEROctetString instance) {
            try {
                innerValue = ASN1Primitive.fromByteArray(instance.getOctets());
            } catch (final IOException e) {
                LOGGER.debug("Failed to parse {} extension value as ASN1: {}", matchKey, value);
                return false;
            }
        } else {
            LOGGER.debug("Expected nested bit string value for extension {}, was: {}", matchKey, value);
            return false;
        }

        if (innerValue instanceof final DEROctetString octetString) {
            val readBytes = new ByteArray(octetString.getOctets());
            return matchBytes.equals(readBytes);
        } else {
            LOGGER.debug("Expected nested bit string value for extension {}, was: {}", matchKey, value);
            return false;
        }
    }
}

package org.apereo.cas.util.jwt;

import module java.base;
import org.apereo.cas.util.crypto.IdentifiableKey;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.JsonWebEncryption;

/**
 * This is {@link JsonWebTokenEncryptor}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SuperBuilder
@Slf4j
public class JsonWebTokenEncryptor {
    /**
     * Allow all algorithms except none.
     */
    public static final List<String> ALGORITHM_ALL_EXCEPT_NONE = List.of("*");

    private final String algorithm;

    @Builder.Default
    private final Map<String, Object> headers = new LinkedHashMap<>();

    private final Key key;

    @Builder.Default
    private final Set<String> allowedAlgorithms = new LinkedHashSet<>();

    @Builder.Default
    private final Set<String> allowedContentEncryptionAlgorithms = new LinkedHashSet<>();

    @Builder.Default
    private final String keyId = UUID.randomUUID().toString();

    private String encryptionMethod;

    /**
     * Encrypt object value.
     *
     * @param payload the payload
     * @return the string
     */
    public String encrypt(final Serializable payload) {
        try {
            val jwe = new JsonWebEncryption();
            jwe.setPayload(payload.toString());
            jwe.setAlgorithmHeaderValue(this.algorithm);
            jwe.setEncryptionMethodHeaderParameter(this.encryptionMethod);

            jwe.setAlgorithmConstraints(getAlgorithmConstraints());
            jwe.setContentEncryptionAlgorithmConstraints(getContentEncryptionAlgorithmConstraints());
            jwe.setContentTypeHeaderValue("JWT");
            jwe.setHeader("typ", "JWT");
            if (this.key instanceof final IdentifiableKey idk) {
                jwe.setKeyIdHeaderValue(idk.getId());
                jwe.setKey(idk.getKey());
            } else {
                FunctionUtils.doIfNotNull(this.keyId, jwe::setKeyIdHeaderValue);
                jwe.setKey(this.key);
            }
            headers.forEach((name, v) -> jwe.setHeader(name, v.toString()));
            LOGGER.trace("Encrypting via [{}]", encryptionMethod);
            return jwe.getCompactSerialization();
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private AlgorithmConstraints getAlgorithmConstraints() {
        return allowedAlgorithms.isEmpty() || allowedAlgorithms.contains("*")
            ? AlgorithmConstraints.DISALLOW_NONE
            : new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
            allowedAlgorithms.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
    }

    private AlgorithmConstraints getContentEncryptionAlgorithmConstraints() {
        return allowedContentEncryptionAlgorithms.isEmpty() || allowedContentEncryptionAlgorithms.contains("*")
            ? AlgorithmConstraints.DISALLOW_NONE
            : new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
            allowedContentEncryptionAlgorithms.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
    }

}

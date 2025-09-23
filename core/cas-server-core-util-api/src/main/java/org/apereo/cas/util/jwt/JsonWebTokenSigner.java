package org.apereo.cas.util.jwt;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.IdentifiableKey;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This is {@link JsonWebTokenSigner}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SuperBuilder
@Slf4j
public class JsonWebTokenSigner {
    /**
     * Allow all algorithms except none.
     */
    public static final Set<String> ALGORITHM_ALL_EXCEPT_NONE = Set.of("*");

    @Builder.Default
    private final String keyId = UUID.randomUUID().toString();

    private final String algorithm;

    @Builder.Default
    private final String mediaType = "JWT";
    
    @Builder.Default
    private final Map<String, Object> headers = new LinkedHashMap<>();

    private final Key key;

    @Builder.Default
    private final Set<String> allowedAlgorithms = new LinkedHashSet<>();

    /**
     * Sign byte array.
     *
     * @param value the value
     * @return the byte []
     */
    public byte[] sign(final byte[] value) {
        return Unchecked.supplier(() -> {
            val base64 = EncodingUtils.encodeUrlSafeBase64(value);
            return sign(base64, true).getBytes(StandardCharsets.UTF_8);
        }).get();
    }

    /**
     * Sign claims.
     *
     * @param claims the claims
     * @return the string
     */
    public String sign(final JwtClaims claims) {
        return Unchecked.supplier(() -> {
            val jsonClaims = claims.toJson();
            return sign(jsonClaims, false);
        }).get();
    }

    private String sign(final String payload, final boolean encoded) throws Exception {
        val jws = new JsonWebSignature();
        if (encoded) {
            jws.setEncodedPayload(payload);
        } else {
            jws.setPayload(payload);
        }
        jws.setAlgorithmHeaderValue(this.algorithm);
        jws.setAlgorithmConstraints(getAlgorithmConstraints());
        jws.setHeader("typ", mediaType);

        if (this.key instanceof final IdentifiableKey idk) {
            jws.setKey(idk.getKey());
            jws.setKeyIdHeaderValue(idk.getId());
        } else {
            jws.setKey(key);
            FunctionUtils.doIfNotNull(this.keyId, jws::setKeyIdHeaderValue);
        }
        headers.forEach((header, value) -> jws.setHeader(header, value.toString()));
        LOGGER.trace("Signing ID token with key id header value [{}] and algorithm header value [{}]",
            jws.getKeyIdHeaderValue(), jws.getAlgorithmHeaderValue());
        return jws.getCompactSerialization();
    }

    private AlgorithmConstraints getAlgorithmConstraints() {
        return allowedAlgorithms.isEmpty() || allowedAlgorithms.contains("*")
            ? AlgorithmConstraints.DISALLOW_NONE
            : new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
            allowedAlgorithms.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
    }

}

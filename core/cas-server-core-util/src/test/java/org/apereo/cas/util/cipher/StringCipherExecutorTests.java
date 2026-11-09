package org.apereo.cas.util.cipher;

import module java.base;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StringCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Cipher")
class StringCipherExecutorTests {

    @Test
    void verifyOperation() {
        val publicKey = """
            {
                "kty": "RSA",
                "e": "AQAB",
                "kid": "kid",
                "alg": "RS256",
                "n": "xqiJDPhYH2nUveZO-5XWcUxNkHg9ff3QplMQYrNncwFrJ5G9YjlNLYpwkEHKVLU5D4mXIbHKDCV00lT71HqiUaJ_9
                XJapoCxrem6dDDdbc_scTeYuBJUYsJCNsa4r38CEgoyRI1jwuyyhvdwj2yiQ04qlbyVgezkGcJB-rEG15p0DMLS-H6DMCcsh6B7XRe2Y1-dhHfQ5HaHCchHMdoUhHhE-xtHWxY2DvjnCLJAwIw6JPvyLraU
                2VtSuEbq6vcXQXSIVF4nHEILigoCqz_ibDWf3112F6C6Km7b3skD4HoBAKAAAMytFPoUphCCBh8e1VaY8r9dsrjKsS-y1N_9XQ"
            }""";

        val cipher = new BaseStringCipherExecutor(publicKey, publicKey, true, true, 512, 256) {
        };
        assertNotNull(cipher.getEncryptionKeySetting());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getSigningKey());
        assertNotNull(cipher.getEncryptionKey());
    }

    @Test
    void verifyPublicSigningKeyOnlyVerifies() throws Exception {
        val jwk = RsaJwkGenerator.generateJwk(2048);
        val signer = new BaseStringCipherExecutor(null, jwk.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE), false, true, 0, 0) {
        };
        val verifier = new BaseStringCipherExecutor(null, jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY), false, true, 0, 0) {
        };
        assertInstanceOf(PublicKey.class, verifier.getSigningKey());
        val encoded = signer.encode("value", ArrayUtils.EMPTY_OBJECT_ARRAY);
        assertEquals("value", verifier.decode(encoded, ArrayUtils.EMPTY_OBJECT_ARRAY));

        verifier.setSigningKey(null);
        assertNull(verifier.decode(encoded, ArrayUtils.EMPTY_OBJECT_ARRAY));
    }
}

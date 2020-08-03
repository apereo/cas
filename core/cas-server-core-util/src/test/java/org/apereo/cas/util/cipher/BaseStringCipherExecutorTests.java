package org.apereo.cas.util.cipher;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseStringCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class BaseStringCipherExecutorTests {

    @Test
    public void verifyOperation() {
        val publicKey = "{\n"
            + "    \"kty\": \"RSA\",\n"
            + "    \"e\": \"AQAB\",\n"
            + "    \"kid\": \"kid\",\n"
            + "    \"alg\": \"RS256\",\n"
            + "    \"n\": \"xqiJDPhYH2nUveZO-5XWcUxNkHg9ff3QplMQYrNncwFrJ5G9YjlNLYpwkEHKVLU5D4mXIbHKDCV00lT71HqiUaJ_9"
            + "XJapoCxrem6dDDdbc_scTeYuBJUYsJCNsa4r38CEgoyRI1jwuyyhvdwj2yiQ04qlbyVgezkGcJB-rEG15p0DMLS-H6DMCcsh"
            + "6B7XRe2Y1-dhHfQ5HaHCchHMdoUhHhE-xtHWxY2DvjnCLJAwIw6JPvyLraU2VtSuEbq6vc"
            + "XQXSIVF4nHEILigoCqz_ibDWf3112F6C6Km7b3skD4HoBAKAAAMytFPoUphCCBh8e1VaY8r9dsrjKsS-y1N_9XQ\"\n"
            + '}';

        val cipher = new BaseStringCipherExecutor(publicKey, publicKey, true, true, 512, 256) {
        };
        assertNotNull(cipher.getEncryptionKeySetting());
        assertNotNull(cipher.getSigningKeySetting());
        assertNotNull(cipher.getSigningKey());
        assertNotNull(cipher.getSecretKeyEncryptionKey());
    }
}

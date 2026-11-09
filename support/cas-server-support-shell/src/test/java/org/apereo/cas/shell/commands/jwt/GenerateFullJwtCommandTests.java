package org.apereo.cas.shell.commands.jwt;

import module java.base;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GenerateFullJwtCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SHELL")
class GenerateFullJwtCommandTests extends BaseCasShellCommandTests {
    /**
     * Without a keystore the JWT would be unsigned, which CAS does not support, so it is refused.
     */
    @Test
    void verifyUnsignedIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new GenerateFullJwtCommand().generateKey(StringUtils.EMPTY,
            "https://localhost:8443/cas/oidc", "{'name':'CAS','clients':['1234']}", "CAS", "300", "casuser"));
    }

    @Test
    void verifySigned() throws Throwable {
        val jwks = new ClassPathResource("jwks.json").getFile().getAbsolutePath();
        assertDoesNotThrow(() -> runShellCommand(() -> "generate-full-jwt --sub=casuser "
                                                       + "--claims={'client_id':'client'} "
                                                       + "--jwks=" + jwks));
        val token = new GenerateFullJwtCommand().generateKey(jwks, "https://localhost:8443/cas/oidc",
            "{'client_id':'client'}", "CAS", "300", "casuser");
        val header = new String(Base64.getUrlDecoder().decode(token.substring(0, token.indexOf('.'))), StandardCharsets.UTF_8);
        assertTrue(header.contains("RS512"));
    }

    @Test
    void verifySignedNeverExpires() {
        assertDoesNotThrow(() -> {
            val jwks = new ClassPathResource("jwks.json").getFile().getAbsolutePath();
            runShellCommand(() -> "generate-full-jwt --sub=casuser "
                                  + "--exp=INFINITE "
                                  + "--aud=client "
                                  + "--iss=https://localhost:8443/cas/oidc "
                                  + "--claims={'client_id':'client'} "
                                  + "--jwks=" + jwks);
        });
    }
}


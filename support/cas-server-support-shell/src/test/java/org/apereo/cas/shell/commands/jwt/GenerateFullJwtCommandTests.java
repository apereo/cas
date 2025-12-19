package org.apereo.cas.shell.commands.jwt;

import module java.base;
import org.apereo.cas.shell.commands.BaseCasShellCommandTests;
import lombok.val;
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
    @Test
    void verifyPlain() {
        assertDoesNotThrow(() -> runShellCommand(() -> "generate-full-jwt --sub=casuser --claims={'name':'CAS','clients':['1234']}"));
    }

    @Test
    void verifySigned() {
        assertDoesNotThrow(() -> {
            val jwks = new ClassPathResource("jwks.json").getFile().getAbsolutePath();
            runShellCommand(() -> "generate-full-jwt --sub=casuser "
                                  + "--claims={'client_id':'client'} "
                                  + "--jwks=" + jwks);
        });
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


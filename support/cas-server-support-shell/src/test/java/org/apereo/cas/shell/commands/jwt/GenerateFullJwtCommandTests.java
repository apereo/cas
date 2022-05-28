package org.apereo.cas.shell.commands.jwt;

import org.apereo.cas.shell.commands.BaseCasShellCommandTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GenerateFullJwtCommandTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableAutoConfiguration
@Tag("SHELL")
public class GenerateFullJwtCommandTests extends BaseCasShellCommandTests {
    @Test
    public void verifyPlain() {
        assertDoesNotThrow(() -> {
            val result = shell.evaluate(() -> "generate-full-jwt --sub casuser --claims {'name':'CAS','clients':['1234']}");
            assertNotNull(result);
        });
    }

    @Test
    public void verifySigned() throws Exception {
        assertDoesNotThrow(() -> {
            val jwks = new ClassPathResource("jwks.json").getFile().getAbsolutePath();
            val result = shell.evaluate(() -> "generate-full-jwt --sub casuser "
                                              + "--claims {'client_id':'client'} "
                                              + "--jwks " + jwks);
            assertNotNull(result);
        });
    }

    @Test
    public void verifySignedNeverExpires() throws Exception {
        assertDoesNotThrow(() -> {
            val jwks = new ClassPathResource("jwks.json").getFile().getAbsolutePath();
            val result = shell.evaluate(() -> "generate-full-jwt --sub casuser "
                                              + "--exp INFINITE "
                                              + "--aud client "
                                              + "--iss https://localhost:8443/cas/oidc "
                                              + "--claims {'client_id':'client'} "
                                              + "--jwks " + jwks);
            assertNotNull(result);
        });
    }
}


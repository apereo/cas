package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.jaas.JaasAuthenticationHandler;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class JaasAuthenticationHandlerSystemConfigurationTests {

    private static final String USERNAME = "test";

    private JaasAuthenticationHandler handler;

    @BeforeEach
    public void initialize() throws Exception {
        val resource = new ClassPathResource("jaas-system.conf");
        val fileName = new File(System.getProperty("java.io.tmpdir"), "jaas-system.conf");
        try (val writer = Files.newBufferedWriter(fileName.toPath(), StandardCharsets.UTF_8)) {
            IOUtils.copy(resource.getInputStream(), writer, Charset.defaultCharset());
            writer.flush();
        }
        if (fileName.exists()) {
            System.setProperty("java.security.auth.login.config", '=' + fileName.getCanonicalPath());
            this.handler = new JaasAuthenticationHandler("", null, null, null);
        }
    }

    @Test
    public void verifyWithAlternativeRealm() throws Exception {
        this.handler.setRealm("TEST");
        assertThrows(LoginException.class, () -> {
            this.handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(USERNAME, "test1"));
        });
    }

    @Test
    public void verifyWithAlternativeRealmAndValidCredentials() throws Exception {
        this.handler.setRealm("TEST");
        assertNotNull(this.handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(USERNAME, USERNAME)));
    }

    @Test
    public void verifyWithValidCredentials() throws Exception {
        assertNotNull(this.handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyWithInvalidCredentials() throws Exception {
        assertThrows(LoginException.class, () -> {
            this.handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(USERNAME, "test1"));
        });
    }
}

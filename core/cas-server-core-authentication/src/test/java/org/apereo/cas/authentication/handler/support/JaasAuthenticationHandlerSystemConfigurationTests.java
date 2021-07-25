package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.jaas.JaasAuthenticationHandler;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
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
@Tag("AuthenticationHandler")
public class JaasAuthenticationHandlerSystemConfigurationTests {

    private static final String USERNAME = "test";

    private JaasAuthenticationHandler handler;

    @BeforeEach
    @SneakyThrows
    public void initialize() {
        val resource = new ClassPathResource("jaas-system.conf");
        val fileName = new File(FileUtils.getTempDirectoryPath(), "jaas-system.conf");
        try (val writer = Files.newBufferedWriter(fileName.toPath(), StandardCharsets.UTF_8)) {
            IOUtils.copy(resource.getInputStream(), writer, Charset.defaultCharset());
            writer.flush();
        }
        if (fileName.exists()) {
            System.setProperty("java.security.auth.login.config", '=' + fileName.getCanonicalPath());
            handler = new JaasAuthenticationHandler(StringUtils.EMPTY, null, null, null);
            handler.setKerberosKdcSystemProperty("P1");
            handler.setKerberosRealmSystemProperty("P2");
        }
    }

    @Test
    public void verifyWithAlternativeRealm() {
        handler.setRealm("TEST");
        assertThrows(LoginException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(USERNAME, "test1")));
    }

    @Test
    public void verifyWithAlternativeRealmAndValidCredentials() throws Exception {
        handler.setRealm("TEST");
        assertNotNull(handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(USERNAME, USERNAME)));
    }

    @Test
    public void verifyWithValidCredentials() throws Exception {
        assertNotNull(handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void verifyWithInvalidCredentials() {
        assertThrows(LoginException.class,
            () -> this.handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(USERNAME, "test1")));
    }
}

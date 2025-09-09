package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.jaas.JaasAuthenticationHandler;
import org.apereo.cas.authentication.principal.Service;

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
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Tag("AuthenticationHandler")
class JaasAuthenticationHandlerSystemConfigurationTests {

    private static final String USERNAME = "test";

    private JaasAuthenticationHandler handler;

    @BeforeEach
    void initialize() throws Exception {
        val resource = new ClassPathResource("jaas-system.conf");
        val fileName = new File(FileUtils.getTempDirectoryPath(), "jaas-system.conf");
        try (val writer = Files.newBufferedWriter(fileName.toPath(), StandardCharsets.UTF_8)) {
            IOUtils.copy(resource.getInputStream(), writer, Charset.defaultCharset());
            writer.flush();
        }
        if (fileName.exists()) {
            System.setProperty("java.security.auth.login.config", '=' + fileName.getCanonicalPath());
            handler = new JaasAuthenticationHandler(StringUtils.EMPTY, null, null);
            handler.setKerberosKdcSystemProperty("P1");
            handler.setKerberosRealmSystemProperty("P2");
        }
    }

    @Test
    void verifyWithAlternativeRealm() {
        handler.setRealm("TEST");
        assertThrows(LoginException.class,
            () -> handler.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(USERNAME, "test1"), mock(Service.class)));
    }

    @Test
    void verifyWithAlternativeRealmAndValidCredentials() throws Throwable {
        handler.setRealm("TEST");
        assertNotNull(handler.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(USERNAME, USERNAME), mock(Service.class)));
    }

    @Test
    void verifyWithValidCredentials() throws Throwable {
        assertNotNull(handler.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));
    }

    @Test
    void verifyWithInvalidCredentials() {
        assertThrows(LoginException.class,
            () -> handler.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(USERNAME, "test1"), mock(Service.class)));
    }
}

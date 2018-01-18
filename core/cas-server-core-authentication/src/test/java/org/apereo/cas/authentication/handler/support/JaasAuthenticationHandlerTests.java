package org.apereo.cas.authentication.handler.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Slf4j
public class JaasAuthenticationHandlerTests {

    private static final String USERNAME = "test";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private JaasAuthenticationHandler handler;

    @Before
    public void setUp() throws Exception {
        final ClassPathResource resource = new ClassPathResource("jaas.conf");
        final File fileName = new File(System.getProperty("java.io.tmpdir"), "jaas.conf");
        try (Writer writer = Files.newBufferedWriter(fileName.toPath(), StandardCharsets.UTF_8)) {
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
        this.thrown.expect(LoginException.class);

        this.handler.setRealm("TEST");
        this.handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(USERNAME, "test1"));
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
        this.thrown.expect(LoginException.class);

        this.handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword(USERNAME, "test1"));
    }
}

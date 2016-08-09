package org.apereo.cas.authentication.handler.support;

import javax.security.auth.login.LoginException;


import org.apache.commons.io.IOUtils;
import org.apereo.cas.authentication.TestUtils;
import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileWriter;

import static org.junit.Assert.*;

/**
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class JaasAuthenticationHandlerTests {

    private transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private JaasAuthenticationHandler handler;

    @Before
    public void setUp() throws Exception {
        final ClassPathResource resource = new ClassPathResource("jaas.conf");
        final File fileName = new File(System.getProperty("java.io.tmpdir"), "jaas.conf");
        try(FileWriter writer = new FileWriter(fileName)) {
            IOUtils.copy(resource.getInputStream(), writer);
            writer.flush();
        }
        if (fileName.exists()) {
            System.setProperty("java.security.auth.login.config", '=' + fileName.getCanonicalPath());
            this.handler = new JaasAuthenticationHandler();
        }
    }

    @Test(expected = LoginException.class)
    public void verifyWithAlternativeRealm() throws Exception {

        this.handler.setRealm("TEST");
        this.handler.authenticate(TestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test1"));
    }

    @Test
    public void verifyWithAlternativeRealmAndValidCredentials() throws Exception {
        this.handler.setRealm("TEST");
        assertNotNull(this.handler.authenticate(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test")));
    }

    @Test
    public void verifyWithValidCredenials() throws Exception {
        assertNotNull(this.handler.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test(expected = LoginException.class)
    public void verifyWithInvalidCredentials() throws Exception {
        this.handler.authenticate(TestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test1"));
    }

}

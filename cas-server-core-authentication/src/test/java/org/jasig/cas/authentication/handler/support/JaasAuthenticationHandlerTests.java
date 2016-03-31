package org.jasig.cas.authentication.handler.support;

import javax.security.auth.login.LoginException;


import org.jasig.cas.authentication.TestUtils;
import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class JaasAuthenticationHandlerTests {

    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private JaasAuthenticationHandler handler;

    @Before
    public void setUp() throws Exception {
        String pathPrefix = System.getProperty("user.dir");
        pathPrefix = !pathPrefix.contains("cas-server-core") ? pathPrefix
            + "/cas-server-core" : pathPrefix;
        logger.info("PATH PREFIX: {}", pathPrefix);

        final String pathToConfig = pathPrefix
            + "/src/test/resources/org/jasig/cas/authentication/handler/support/jaas.conf";
        System.setProperty("java.security.auth.login.config", '=' +pathToConfig);
        this.handler = new JaasAuthenticationHandler();
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

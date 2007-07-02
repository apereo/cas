/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.handler.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.TestUtils;

import junit.framework.TestCase;

public class JaasAuthenticationHandlerTests extends TestCase {

    private final Log log = LogFactory.getLog(this.getClass());

    private JaasAuthenticationHandler handler;

    protected void setUp() throws Exception {
        String pathPrefix = System.getProperty("user.dir");
        pathPrefix = !pathPrefix.contains("cas-server-core") ? pathPrefix
            + "/cas-server-core" : pathPrefix;
        log.info("PATH PREFIX: " + pathPrefix);

        final String pathToConfig = pathPrefix
            + "/src/test/resources/org/jasig/cas/authentication/handler/support/jaas.conf";
        System.setProperty("java.security.auth.login.config", "="+pathToConfig); 
        this.handler = new JaasAuthenticationHandler();
    }

    public void testWithAlternativeRealm() throws Exception {

        this.handler.setRealm("TEST");
        assertFalse(this.handler.authenticate(TestUtils
            .getCredentialsWithDifferentUsernameAndPassword("test", "test1")));
    }

    public void testWithAlternativeRealmAndValidCredentials() throws Exception {
        this.handler.setRealm("TEST");
        assertTrue(this.handler.authenticate(TestUtils
            .getCredentialsWithDifferentUsernameAndPassword("test", "test")));
    }

    public void testWithValidCredenials() throws Exception {
        assertTrue(this.handler.authenticate(TestUtils
            .getCredentialsWithSameUsernameAndPassword()));
    }

    public void testWithInvalidCredentials() throws Exception {
        assertFalse(this.handler.authenticate(TestUtils
            .getCredentialsWithDifferentUsernameAndPassword("test", "test1")));
    }

}

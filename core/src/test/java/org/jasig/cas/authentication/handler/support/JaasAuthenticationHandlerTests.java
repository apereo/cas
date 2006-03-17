/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
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
        pathPrefix = pathPrefix.indexOf("/doc") == -1 ? pathPrefix : pathPrefix.substring(0, pathPrefix.indexOf("/doc")-1); 
        log.info("PATH PREFIX: " + pathPrefix);
        
        final String pathToConfig = pathPrefix + "/core/src/test/resources/org/jasig/cas/authentication/handler/support/jaas.conf";
        System.setProperty("java.security.auth.login.config", pathToConfig);
        this.handler = new JaasAuthenticationHandler();
        this.handler.afterPropertiesSet();
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

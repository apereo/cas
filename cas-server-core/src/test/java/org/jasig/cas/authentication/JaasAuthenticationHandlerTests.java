/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication;

import java.security.GeneralSecurityException;

import junit.framework.TestCase;
import org.jasig.cas.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaasAuthenticationHandlerTests extends TestCase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private JaasAuthenticationHandler handler;

    protected void setUp() throws Exception {
        String pathPrefix = System.getProperty("user.dir");
        pathPrefix = !pathPrefix.contains("cas-server-core") ? pathPrefix
            + "/cas-server-core" : pathPrefix;
        log.info("PATH PREFIX: " + pathPrefix);

        final String pathToConfig = pathPrefix
            + "/src/test/resources/org/jasig/cas/authentication/jaas.conf";
        System.setProperty("java.security.auth.login.config", "="+pathToConfig); 
        this.handler = new JaasAuthenticationHandler();
    }

    public void testWithAlternativeRealm() throws Exception {

        this.handler.setRealm("TEST");
        try {
            this.handler.authenticate(TestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test1"));
            fail("Authentication succeeded when it should have failed.");
        } catch (GeneralSecurityException e) {}
    }

    public void testWithAlternativeRealmAndValidCredentials() throws Exception {
        this.handler.setRealm("TEST");
        final HandlerResult result = this.handler.authenticate(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test"));
        assertEquals(this.handler.getName(), result.getHandlerName());
    }

    public void testWithValidCredenials() throws Exception {
        final HandlerResult result = this.handler.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals(this.handler.getName(), result.getHandlerName());
    }

    public void testWithInvalidCredentials() throws Exception {
        try {
            this.handler.authenticate(TestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test1"));
            fail("Authentication succeeded when it should have failed.");
        } catch (GeneralSecurityException e) {}
    }

}

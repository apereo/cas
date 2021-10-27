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
package org.jasig.cas.authentication.handler.support;

import javax.security.auth.login.LoginException;

import org.jasig.cas.TestUtils;
import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class JaasAuthenticationHandlerTests {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private JaasAuthenticationHandler handler;

    @Before
    public void setUp() throws Exception {
        String pathPrefix = System.getProperty("user.dir");
        pathPrefix = !pathPrefix.contains("cas-server-core") ? pathPrefix
            + "/cas-server-core" : pathPrefix;
        logger.info("PATH PREFIX: {}", pathPrefix);

        final String pathToConfig = pathPrefix
            + "/src/test/resources/org/jasig/cas/authentication/handler/support/jaas.conf";
        System.setProperty("java.security.auth.login.config", "="+pathToConfig);
        this.handler = new JaasAuthenticationHandler();
    }

    @Test(expected = LoginException.class)
    public void testWithAlternativeRealm() throws Exception {

        this.handler.setRealm("TEST");
        this.handler.authenticate(TestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test1"));
    }

    @Test
    public void testWithAlternativeRealmAndValidCredentials() throws Exception {
        this.handler.setRealm("TEST");
        assertNotNull(this.handler.authenticate(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test")));
    }

    @Test
    public void testWithValidCredenials() throws Exception {
        assertNotNull(this.handler.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test(expected = LoginException.class)
    public void testWithInvalidCredentials() throws Exception {
        this.handler.authenticate(TestUtils.getCredentialsWithDifferentUsernameAndPassword("test", "test1"));
    }

}

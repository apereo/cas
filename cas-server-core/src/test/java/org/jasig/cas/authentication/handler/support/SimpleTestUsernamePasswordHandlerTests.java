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

import static org.junit.Assert.*;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.PlainTextPasswordEncoder;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.Test;

/**
 * Test of the simple username/password handler.
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class SimpleTestUsernamePasswordHandlerTests {

    private SimpleTestUsernamePasswordAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();
        this.authenticationHandler.setPasswordEncoder(new PlainTextPasswordEncoder());
    }

    @Test
    public void testSupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(TestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void testDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(TestUtils.getHttpBasedServiceCredentials()));
    }

    @Test
    public void testValidUsernamePassword() throws AuthenticationException {
        assertTrue(this.authenticationHandler.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void testInvalidUsernamePassword() {
        try {
            assertFalse(this.authenticationHandler.authenticate(TestUtils
                    .getCredentialsWithDifferentUsernameAndPassword()));
        } catch (final AuthenticationException ae) {
            ae.printStackTrace();
        }
    }

    @Test
    public void testNullUsernamePassword() {
        try {
            assertFalse(this.authenticationHandler.authenticate(TestUtils
                    .getCredentialsWithSameUsernameAndPassword(null)));
        } catch (final AuthenticationException ae) {
            ae.printStackTrace();
        }
    }

    @Test
    public void testAlternateClass() {
        this.authenticationHandler.setClassToSupport(UsernamePasswordCredentials.class);
        assertTrue(this.authenticationHandler.supports(new UsernamePasswordCredentials()));
    }

    @Test
    public void testAlternateClassWithSubclassSupport() {
        this.authenticationHandler.setClassToSupport(UsernamePasswordCredentials.class);
        this.authenticationHandler.setSupportSubClasses(true);
        assertTrue(this.authenticationHandler.supports(new ExtendedCredentials()));
    }

    @Test
    public void testAlternateClassWithNoSubclassSupport() {
        this.authenticationHandler.setClassToSupport(UsernamePasswordCredentials.class);
        this.authenticationHandler.setSupportSubClasses(false);
        assertFalse(this.authenticationHandler.supports(new ExtendedCredentials()));
    }

    protected class ExtendedCredentials extends UsernamePasswordCredentials {

        private static final long serialVersionUID = 406992293105518363L;
        // nothing to see here
    }
}

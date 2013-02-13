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

import javax.security.auth.login.FailedLoginException;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.HandlerResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test of the simple username/password handler
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class SimpleTestUsernamePasswordHandlerTests {

    private SimpleTestUsernamePasswordAuthenticationHandler authenticationHandler;

    @Before
    public void setUp() throws Exception {
        this.authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();
    }

    @Test
    public void testSupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(TestUtils
            .getCredentialsWithSameUsernameAndPassword()));
    }

    @Test
    public void testDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(TestUtils
            .getHttpBasedServiceCredentials()));
    }

    @Test
    public void testValidUsernamePassword() throws Exception {
        final HandlerResult result = this.authenticationHandler.authenticate(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals(this.authenticationHandler.getName(), result.getHandlerName());
    }

    @Test(expected = FailedLoginException.class)
    public void testInvalidUsernamePassword() throws Exception {
        this.authenticationHandler.authenticate(TestUtils.getCredentialsWithDifferentUsernameAndPassword());
    }

    @Test(expected = FailedLoginException.class)
    public void testNullUsernamePassword() throws Exception {
        this.authenticationHandler.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword(null));
    }
}
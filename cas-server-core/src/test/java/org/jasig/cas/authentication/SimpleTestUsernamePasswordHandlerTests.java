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
import org.jasig.cas.authentication.support.PlainTextPasswordEncoder;

/**
 * Test of the simple username/password handler
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class SimpleTestUsernamePasswordHandlerTests extends TestCase {

    private SimpleTestUsernamePasswordAuthenticationHandler authenticationHandler;

    protected void setUp() throws Exception {
        this.authenticationHandler = new SimpleTestUsernamePasswordAuthenticationHandler();
        this.authenticationHandler.setPasswordEncoder(new PlainTextPasswordEncoder());
    }

    public void testSupportsProperUserCredentials() {
        assertTrue(this.authenticationHandler.supports(TestUtils.getCredentialsWithSameUsernameAndPassword()));
    }

    public void testDoesntSupportBadUserCredentials() {
        assertFalse(this.authenticationHandler.supports(TestUtils.getHttpBasedServiceCredentials()));
    }

    public void testValidUsernamePassword() throws Exception {
        try {
            final HandlerResult result = this.authenticationHandler.authenticate(
                    TestUtils.getCredentialsWithSameUsernameAndPassword());
            assertEquals(this.authenticationHandler.getName(), result.getHandlerName());
        } catch (GeneralSecurityException e) {
            fail("Authentication failed when it should have succeeded.");
        }
    }

    public void testInvalidUsernamePassword() throws Exception {
        try {
            this.authenticationHandler.authenticate(TestUtils.getCredentialsWithDifferentUsernameAndPassword());
            fail("Authentication succeeded when it should have failed.");
        } catch (GeneralSecurityException e) {}
    }

    public void testNullUsernamePassword() throws Exception {
        try {
            this.authenticationHandler.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword(null));
            fail("Authentication succeeded when it should have failed.");
        } catch (GeneralSecurityException e) {}
    }
    
    public void testAlternateClass() {
        this.authenticationHandler.setClassToSupport(UsernamePasswordCredential.class);
        assertTrue(this.authenticationHandler.supports(new UsernamePasswordCredential()));
    }
    
    public void testAlternateClassWithSubclassSupport() {
        this.authenticationHandler.setClassToSupport(UsernamePasswordCredential.class);
        this.authenticationHandler.setSupportSubClasses(true);
        assertTrue(this.authenticationHandler.supports(new ExtendedCredential()));
    }
    
    public void testAlternateClassWithNoSubclassSupport() {
        this.authenticationHandler.setClassToSupport(UsernamePasswordCredential.class);
        this.authenticationHandler.setSupportSubClasses(false);
        assertFalse(this.authenticationHandler.supports(new ExtendedCredential()));
    }
    
    protected class ExtendedCredential extends UsernamePasswordCredential {

        private static final long serialVersionUID = 406992293105518363L;
        // nothing to see here
    }
}
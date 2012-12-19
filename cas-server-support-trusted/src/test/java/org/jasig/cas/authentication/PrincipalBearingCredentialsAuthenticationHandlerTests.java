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

import org.jasig.cas.authentication.PrincipalBearingCredential;
import org.jasig.cas.authentication.PrincipalBearingCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.SimplePrincipal;
import org.jasig.cas.authentication.UsernamePasswordCredential;

import junit.framework.TestCase;

/**
 * @author Andrew Petro
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class PrincipalBearingCredentialsAuthenticationHandlerTests 
    extends TestCase {

    private PrincipalBearingCredentialsAuthenticationHandler handler =
            new PrincipalBearingCredentialsAuthenticationHandler();

    /**
     * When the credentials bear a Principal, succeed the authentication.
     */
    public void testNonNullPrincipal() throws Exception {
        PrincipalBearingCredential credentials = new PrincipalBearingCredential(new SimplePrincipal("scott"));
        assertNotNull(this.handler.authenticate(credentials));
    }    
    
    public void testSupports() {
        PrincipalBearingCredential credentials = new PrincipalBearingCredential(new SimplePrincipal("scott"));
        assertTrue(this.handler.supports(credentials));
        assertFalse(this.handler.supports(new UsernamePasswordCredential()));
    }
}

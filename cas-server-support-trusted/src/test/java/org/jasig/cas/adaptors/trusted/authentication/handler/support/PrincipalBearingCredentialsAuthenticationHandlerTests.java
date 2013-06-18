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
package org.jasig.cas.adaptors.trusted.authentication.handler.support;

import static org.junit.Assert.*;

import org.jasig.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.junit.Test;

/**
 * @author Andrew Petro
 * @since 3.0.5
 */
public final class PrincipalBearingCredentialsAuthenticationHandlerTests {

    private final PrincipalBearingCredentialsAuthenticationHandler handler
                = new PrincipalBearingCredentialsAuthenticationHandler();
    /**
     * When the credentials bear a Principal, succeed the authentication.
     */
    @Test
    public void testNonNullPrincipal() throws Exception {
        PrincipalBearingCredential credentials = new PrincipalBearingCredential(new SimplePrincipal("scott"));
        assertNotNull(this.handler.authenticate(credentials));
    }

    @Test
    public void testSupports() {
        PrincipalBearingCredential credentials = new PrincipalBearingCredential(new SimplePrincipal("scott"));
        assertTrue(this.handler.supports(credentials));
        assertFalse(this.handler.supports(new UsernamePasswordCredential()));
    }
}

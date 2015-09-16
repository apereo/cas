/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.support.spnego.authentication.principal;

import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public class SpnegoCredentialsTests {

    @Test
    public void verifyToStringWithNoPrincipal() {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {});

        assertTrue(credentials.toString().contains("unknown"));
    }

    @Test
    public void verifyToStringWithPrincipal() {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {});
        final Principal principal = new DefaultPrincipalFactory().createPrincipal("test");
        credentials.setPrincipal(principal);
        assertEquals("test", credentials.toString());
    }

    /**
     * Important for SPNEGO in particular as the credential will be hashed prior to Principal resolution
     */
    @Test
    public void verifyCredentialsHashSafelyWithoutPrincipal() {
        final SpnegoCredential credential = new SpnegoCredential(new byte[] {});
        final Set<SpnegoCredential> set = new HashSet<>();
        try {
            set.add(credential);
        } catch(final Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Make sure that when the Principal becomes populated / changes we return a new hash
     */
    @Test
    public void verifyPrincipalAffectsHash(){
        final SpnegoCredential credential = new SpnegoCredential(new byte[] {});
        final int hash1 = credential.hashCode();
        final Principal principal = new DefaultPrincipalFactory().createPrincipal("test");
        credential.setPrincipal(principal);
        final int hash2 = credential.hashCode();
        assertNotEquals(hash1, hash2);
    }
}

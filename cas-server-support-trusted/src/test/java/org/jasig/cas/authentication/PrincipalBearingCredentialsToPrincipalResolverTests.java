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
import org.jasig.cas.authentication.PrincipalBearingPrincipalResolver;
import org.jasig.cas.authentication.SimplePrincipal;
import org.jasig.cas.authentication.UsernamePasswordCredential;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 *
 */
public class PrincipalBearingCredentialsToPrincipalResolverTests extends
    TestCase {
    private PrincipalBearingPrincipalResolver resolver;
    
    public void setUp() throws Exception {
        this.resolver = new PrincipalBearingPrincipalResolver();
    }
    
    public void testSupports() {
        assertTrue(this.resolver.supports(new PrincipalBearingCredential(new SimplePrincipal("test"))));
        assertFalse(this.resolver.supports(new UsernamePasswordCredential()));
        assertFalse(this.resolver.supports(null));
    }
    
    public void testReturnedPrincipal() {
        assertEquals("test", this.resolver.resolve(new PrincipalBearingCredential(new SimplePrincipal("test"))).getId());
    }
    
}

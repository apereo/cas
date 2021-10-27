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
package org.jasig.cas.services;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.Principal;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class PrincipalAttributeRegisteredServiceUsernameProviderTests {
    @Test
    public void verifyUsernameByPrincipalAttribute() {
        final PrincipalAttributeRegisteredServiceUsernameProvider provider =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("userid", "u1");
        attrs.put("cn", "TheName");
        
        final Principal p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);
        
        final String id = provider.resolveUsername(p, TestUtils.getService());
        assertEquals(id, "TheName");
        
    }
    
    @Test
    public void verifyUsernameByPrincipalAttributeNotFound() {
        final PrincipalAttributeRegisteredServiceUsernameProvider provider =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("userid", "u1");
                
        final Principal p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);
        
        final String id = provider.resolveUsername(p, TestUtils.getService());
        assertEquals(id, p.getId());
        
    }

    @Test
    public void verifyEquality() {
        final PrincipalAttributeRegisteredServiceUsernameProvider provider =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        final PrincipalAttributeRegisteredServiceUsernameProvider provider2 =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        assertEquals(provider, provider2);
    }
}

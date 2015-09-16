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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class DefaultRegisteredServiceUsernameProviderTests {
    
    @Test
    public void verifyRegServiceUsername() {
        final DefaultRegisteredServiceUsernameProvider provider = 
                new DefaultRegisteredServiceUsernameProvider();
        
        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn("id");
        final String id = provider.resolveUsername(principal, TestUtils.getService());
        assertEquals(id, principal.getId());
    }

    @Test
    public void verifyEquality() {
        final DefaultRegisteredServiceUsernameProvider provider =
                new DefaultRegisteredServiceUsernameProvider();

        final DefaultRegisteredServiceUsernameProvider provider2 =
                new DefaultRegisteredServiceUsernameProvider();

        assertEquals(provider, provider2);
    }
}

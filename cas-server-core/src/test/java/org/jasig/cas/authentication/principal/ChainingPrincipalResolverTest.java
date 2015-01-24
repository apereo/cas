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
package org.jasig.cas.authentication.principal;

import org.jasig.cas.authentication.Credential;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link ChainingPrincipalResolver}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class ChainingPrincipalResolverTest {

    private final PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    @Test
    public void examineSupports() throws Exception {
        final Credential credential = mock(Credential.class);
        when(credential.getId()).thenReturn("a");

        final PrincipalResolver resolver1 = mock(PrincipalResolver.class);
        when(resolver1.supports(eq(credential))).thenReturn(true);

        final PrincipalResolver resolver2 = mock(PrincipalResolver.class);
        when(resolver2.supports(eq(credential))).thenReturn(false);

        final ChainingPrincipalResolver resolver = new ChainingPrincipalResolver();
        resolver.setChain(Arrays.asList(resolver1, resolver2));
        assertTrue(resolver.supports(credential));
    }

    @Test
    public void examineResolve() throws Exception {
        final Credential credential = mock(Credential.class);
        when(credential.getId()).thenReturn("input");

        final PrincipalResolver resolver1 = mock(PrincipalResolver.class);
        when(resolver1.supports(eq(credential))).thenReturn(true);
        when(resolver1.resolve((eq(credential)))).thenReturn(principalFactory.createPrincipal("output"));

        final PrincipalResolver resolver2 = mock(PrincipalResolver.class);
        when(resolver2.supports(any(Credential.class))).thenReturn(false);
        when(resolver2.resolve(argThat(new ArgumentMatcher<Credential>() {
            @Override
            public boolean matches(final Object o) {
                return "output".equals(((Credential) o).getId());
            }
        }))).thenReturn(principalFactory.createPrincipal("final", Collections.<String, Object>singletonMap("mail", "final@example.com")));

        final ChainingPrincipalResolver resolver = new ChainingPrincipalResolver();
        resolver.setChain(Arrays.asList(resolver1, resolver2));
        final Principal principal = resolver.resolve(credential);
        assertEquals("final", principal.getId());
        assertEquals("final@example.com", principal.getAttributes().get("mail"));
    }

}

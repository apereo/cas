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

package org.jasig.cas.authentication.principal;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;


/**
 * Handles tests for {@link DefaultPrincipalAttributesRepository}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultPrincipalAttributesRepositoryTests {
    @Test
    public void testNoAttributes() {
        final PrincipalAttributesRepository rep = new DefaultPrincipalAttributesRepository();
        assertEquals(rep.getAttributes("uid").size(), 0);
    }

    @Test
    public void testInitialAttributes() {
        final PrincipalAttributesRepository rep = new DefaultPrincipalAttributesRepository(
                Collections.<String, Object>singletonMap("mail", "final@example.com")
        );
        assertEquals(rep.getAttributes("uid").size(), 1);
        assertTrue(rep.getAttributes("uid").containsKey("mail"));
    }

    @Test
    public void testAttributesOnSetter() {
        final PrincipalAttributesRepository rep = new DefaultPrincipalAttributesRepository();
        rep.setAttributes(Collections.<String, Object>singletonMap("mail", "final@example.com"));
        assertEquals(rep.getAttributes("uid").size(), 1);
        assertTrue(rep.getAttributes("uid").containsKey("mail"));
    }
}

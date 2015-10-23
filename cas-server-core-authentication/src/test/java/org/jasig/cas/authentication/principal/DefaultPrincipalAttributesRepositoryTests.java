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

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link DefaultPrincipalAttributesRepository}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultPrincipalAttributesRepositoryTests {
    private final PrincipalFactory factory = new DefaultPrincipalFactory();

    @Test
    public void checkNoAttributes() {
        final PrincipalAttributesRepository rep = new DefaultPrincipalAttributesRepository();
        assertEquals(rep.getAttributes(this.factory.createPrincipal("uid")).size(), 0);
    }

    @Test
    public void checkInitialAttributes() {
        final Principal p = this.factory.createPrincipal("uid",
                Collections.<String, Object>singletonMap("mail", "final@example.com"));
        final PrincipalAttributesRepository rep = new DefaultPrincipalAttributesRepository();
        assertEquals(rep.getAttributes(p).size(), 1);
        assertTrue(rep.getAttributes(p).containsKey("mail"));
    }
}

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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public class SimplePrincipalFactoryTests {
    @Test
    public void checkPrincipalCreation() {
        final PrincipalFactory fact = new DefaultPrincipalFactory();
        final Map<String, Object> map = new HashMap<>();
        map.put("a1", "v1");
        map.put("a2", "v3");

        final Principal p = fact.createPrincipal("user", map);
        assertTrue(p instanceof SimplePrincipal);
        assertEquals(p.getAttributes(), map);
    }
}

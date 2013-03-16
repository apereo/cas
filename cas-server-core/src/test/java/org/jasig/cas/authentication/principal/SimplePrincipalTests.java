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

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * @author Scott Battaglia
 * @since 3.0
 */
public final class SimplePrincipalTests  {

    @Test
    public void testProperId() {
        final String id = "test";
        assertEquals(id, new SimplePrincipal(id).getId());
    }

    @Test
    public void testEqualsWithNull() {
        assertFalse(new SimplePrincipal("test").equals(null));
    }

    @Test
    public void testEqualsWithBadClass() {
        assertFalse(new SimplePrincipal("test").equals("test"));
    }

    @Test
    public void testEquals() {
        assertTrue(new SimplePrincipal("test").equals(new SimplePrincipal(
            "test")));
    }
}

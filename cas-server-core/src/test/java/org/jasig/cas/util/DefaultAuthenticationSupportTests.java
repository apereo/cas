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

package org.jasig.cas.util;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Misagh Moayyed
 */
public class DefaultAuthenticationSupportTests {
    @Test
    public void testPrincipalFromTgt() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("attr1", "val1");
        map.put("attr2", "val2");
        final Principal p = TestUtils.getPrincipal("principal", map);

        final Authentication authn = TestUtils.getAuthentication(p);
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getAuthentication()).thenReturn(authn);

        final TicketRegistry registry = mock(TicketRegistry.class);
        when(registry.getTicket(any(String.class))).thenReturn(tgt);
        when(registry.getTicket(any(String.class), any(Class.class))).thenReturn(tgt);

        final AuthenticationSupport support = new DefaultAuthenticationSupport(registry);
        assertEquals(support.getAuthenticationFrom("TGT"), authn);
        assertEquals(support.getAuthenticatedPrincipalFrom("TGT"), p);
        assertEquals(support.getPrincipalAttributesFrom("TGT"), map);
    }
}

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
package org.jasig.cas.web.flow;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.NullPrincipal;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link GenericSuccessViewAction}
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class GenericSuccessViewActionTests {

    @Test
    public void verifyValidPrincipal() throws InvalidTicketException {
        final CentralAuthenticationService cas = mock(CentralAuthenticationService.class);
        final Authentication authn = mock(Authentication.class);
        when(authn.getPrincipal()).thenReturn(TestUtils.getPrincipal("cas"));
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getAuthentication()).thenReturn(authn);



        when(cas.getTicket(any(String.class), any(Ticket.class.getClass()))).thenReturn(tgt);
        final GenericSuccessViewAction action = new GenericSuccessViewAction(cas);
        final Principal p = action.getAuthenticationPrincipal("TGT-1");
        assertNotNull(p);
        assertEquals(p.getId(), "cas");
    }

    @Test
    public void verifyPrincipalCanNotBeDetemined() throws InvalidTicketException {
        final CentralAuthenticationService cas = mock(CentralAuthenticationService.class);
        when(cas.getTicket(any(String.class), any(Ticket.class.getClass()))).thenThrow(new InvalidTicketException("TGT-1"));
        final GenericSuccessViewAction action = new GenericSuccessViewAction(cas);
        final Principal p = action.getAuthenticationPrincipal("TGT-1");
        assertNotNull(p);
        assertTrue(p instanceof NullPrincipal);
    }
}

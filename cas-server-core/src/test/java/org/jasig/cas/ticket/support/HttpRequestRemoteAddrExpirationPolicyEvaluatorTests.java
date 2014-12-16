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
package org.jasig.cas.ticket.support;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.ticket.TicketState;
import org.junit.Test;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class HttpRequestRemoteAddrExpirationPolicyEvaluatorTests {

    @Test
    public void evaluatorMatches() {
        final HttpRequestRemoteAddrExpirationPolicyEvaluator evaluator =
                new HttpRequestRemoteAddrExpirationPolicyEvaluator("1\\.2.+");
        
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("1.2.5.4");
        assertTrue(evaluator.satisfiesTicketExpirationPolicy(request, getTicketState()));
    }
    
    @Test
    public void evaluatorDoesNotMatch() {
        final HttpRequestRemoteAddrExpirationPolicyEvaluator evaluator =
                new HttpRequestRemoteAddrExpirationPolicyEvaluator("111\\.2.+");
        
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("1.2.5.4");
        assertFalse(evaluator.satisfiesTicketExpirationPolicy(request, getTicketState()));
    }
    
    private TicketState getTicketState() {
        final TicketState state = mock(TicketState.class);
        when(state.getAuthentication()).thenReturn(mock(Authentication.class));
        when(state.getCreationTime()).thenReturn(new Date(2013, 1, 1).getTime());
        when(state.getLastTimeUsed()).thenReturn(new Date(2014, 1, 1).getTime());
        return state;
    }
}

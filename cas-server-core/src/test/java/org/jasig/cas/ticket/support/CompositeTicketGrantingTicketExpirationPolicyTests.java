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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Misagh Moayyed
 */
public class CompositeTicketGrantingTicketExpirationPolicyTests {

    @Before
    public void setup() {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }
    
    @Test
    public void testNoEvaluatorSatisfiesPolicy() {
        final Map<TicketExpirationPolicyEvaluator, ExpirationPolicy> evaluators = Collections.emptyMap();
        final ExpirationPolicy policy = new CompositeTicketGrantingTicketExpirationPolicy(evaluators);
        assertTrue(policy.isExpired(getTicketState()));
    }
    
    @Test
    public void testEvaluatorSatisfiesPolicy() {
        final Map<TicketExpirationPolicyEvaluator, ExpirationPolicy> evaluators =
                new HashMap<TicketExpirationPolicyEvaluator, ExpirationPolicy>();
        
        final TicketExpirationPolicyEvaluator evaluator = new TicketExpirationPolicyEvaluator() {
            @Override
            public boolean satisfiesTicketExpirationPolicy(final HttpServletRequest request, final TicketState state) {
                return true;
            }
        };
        evaluators.put(evaluator, new NeverExpiresExpirationPolicy());
        
        final ExpirationPolicy policy = new CompositeTicketGrantingTicketExpirationPolicy(evaluators);
        assertTrue(!policy.isExpired(getTicketState()));
    }
    
    private TicketState getTicketState() {
        final TicketState state = mock(TicketState.class);
        when(state.getAuthentication()).thenReturn(mock(Authentication.class));
        when(state.getCreationTime()).thenReturn(new Date(2013, 1, 1).getTime());
        when(state.getLastTimeUsed()).thenReturn(new Date(2014, 1, 1).getTime());
        return state;
    }
}

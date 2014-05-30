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

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.ticket.TicketState;

/**
 * Defines the set of operations that determine whether the incoming request satisfies a set of
 * implemented rules via {@link #satisfiesTicketExpirationPolicy(HttpServletRequest, TicketState)},
 * such that if it does, the expiration policy of the {@link TicketState} may be handled
 * differently by the caller.
 * 
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface TicketExpirationPolicyEvaluator {
    
    /**
     * Determine whether the policy can be satisfied for the
     * given ticket state and request.
     *
     * @param request the request
     * @param state the state
     * @return true, if the requested policy can be used.
     */
    boolean satisfiesTicketExpirationPolicy(final HttpServletRequest request, final TicketState state);
}

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
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;

import junit.framework.TestCase;

public class InvalidTicketExceptionTests extends TestCase {

    public void testCodeNoThrowable() {
        TicketException t = new InvalidTicketException();
        assertEquals("INVALID_TICKET", t.getCode());
    }

    public void testCodeWithThrowable() {
        AuthenticationException a = new BadCredentialsAuthenticationException();
        TicketException t = new InvalidTicketException(a);

        assertEquals(a.toString(), t.getCode());
    }
}

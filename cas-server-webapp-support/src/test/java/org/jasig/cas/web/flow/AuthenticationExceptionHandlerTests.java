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

import org.jasig.cas.authentication.AuthenticationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.binding.message.MessageContext;

import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(JUnit4.class)
public class AuthenticationExceptionHandlerTests {
 
    @Test
    public void handleAccountNotFoundExceptionByDefefault() {
        final AuthenticationExceptionHandler handler = new AuthenticationExceptionHandler();
        final MessageContext ctx = mock(MessageContext.class);
        
        final Map<String, Class<? extends Exception>> map = new HashMap<>();
        map.put("notFound", AccountNotFoundException.class);
        final String id = handler.handle(new AuthenticationException(map), ctx);
        assertEquals(id, AccountNotFoundException.class.getSimpleName());
    }

    @Test
    public void handleUnknownExceptionByDefefault() {
        final AuthenticationExceptionHandler handler = new AuthenticationExceptionHandler();
        final MessageContext ctx = mock(MessageContext.class);
        
        final Map<String, Class<? extends Exception>> map = new HashMap<>();
        map.put("unknown", GeneralSecurityException.class);
        final String id = handler.handle(new AuthenticationException(map), ctx);
        assertEquals(id, "UNKNOWN");
    }
    
}

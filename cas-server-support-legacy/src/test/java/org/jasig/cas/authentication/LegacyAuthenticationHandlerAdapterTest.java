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
package org.jasig.cas.authentication;

import javax.security.auth.login.FailedLoginException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link LegacyAuthenticationHandlerAdapter}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-context.xml" })
public class LegacyAuthenticationHandlerAdapterTest {

    @Autowired
    @Qualifier("alwaysPassHandler")
    private AuthenticationHandler alwaysPassHandler;

    @Autowired
    @Qualifier("alwaysFailHandler")
    private AuthenticationHandler alwaysFailHandler;

    @Test
    public void verifyAuthenticateSuccess() throws Exception {
        final HandlerResult result = alwaysPassHandler.authenticate(new UsernamePasswordCredential("a", "b"));
        assertEquals("TestAlwaysPassAuthenticationHandler", result.getHandlerName());
    }

    @Test(expected = FailedLoginException.class)
    public void examineAuthenticateFailure() throws Exception {
        alwaysFailHandler.authenticate(new UsernamePasswordCredential("a", "b"));
    }

    @Test
    public void verifySupports() throws Exception {
        assertTrue(alwaysPassHandler.supports(new UsernamePasswordCredential("a", "b")));
        assertTrue(alwaysFailHandler.supports(new UsernamePasswordCredential("a", "b")));
    }

    @Test
    public void verifyGetName() throws Exception {
        assertEquals("TestAlwaysPassAuthenticationHandler", alwaysPassHandler.getName());
        assertEquals("TestAlwaysFailAuthenticationHandler", alwaysFailHandler.getName());
    }
}

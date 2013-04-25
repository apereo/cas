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
package org.jasig.cas.authentication;

import java.util.Properties;

import org.jasig.cas.RequiredConfigurationProfileValueSource;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link LdapAuthenticationHandler}.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext-test.xml"})
@ProfileValueSourceConfiguration(RequiredConfigurationProfileValueSource.class)
@IfProfileValue(name = "authenticationConfig", value = "true")
public class LdapAuthenticationHandlerTests {

    @Autowired
    private LdapAuthenticationHandler handler;

    @Autowired
    @Qualifier("testCredentials")
    private Properties testCredentials;


    @Test
    public void testAuthenticate() throws Exception {
        String [] values;
        String password;
        String expected;
        for (String username : testCredentials.stringPropertyNames()) {
            values = testCredentials.get(username).toString().split("\\|");
            password = values[0];
            expected = values[1];
            if (Boolean.TRUE.toString().equalsIgnoreCase(expected)) {
                final HandlerResult result = this.handler.authenticate(newCredentials(username, password));
                assertEquals(this.handler.getName(), result.getHandlerName());
                assertNotNull(result.getPrincipal());
                assertEquals(username, result.getPrincipal().getId());
            } else {
                try {
                    handler.authenticate(newCredentials(username, password));
                    fail("Should have thrown " + expected);
                } catch (Exception e) {
                    assertEquals(expected, e.getClass().getSimpleName());
                }
            }
        }
    }

    private UsernamePasswordCredentials newCredentials(final String user, final String pass) {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername(user);
        credentials.setPassword(pass);
        return credentials;
    }
}

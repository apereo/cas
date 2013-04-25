/*
  $Id: $

  Copyright (C) 2012 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: $
  Updated: $Date: $
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

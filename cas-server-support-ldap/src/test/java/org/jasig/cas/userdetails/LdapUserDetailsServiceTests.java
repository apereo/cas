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
package org.jasig.cas.userdetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.jasig.cas.RequiredConfigurationProfileValueSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.annotation.ProfileValueSourceConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for the {@link LdapUserDetailsService} class.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext-test.xml"})
@ProfileValueSourceConfiguration(RequiredConfigurationProfileValueSource.class)
@IfProfileValue(name = "userDetailsConfig", value = "true")
public class LdapUserDetailsServiceTests {

    @Autowired
    private LdapUserDetailsService userDetailsService;

    @Autowired
    @Qualifier("testUserDetails")
    private Properties testUserDetails;


    @Test
    public void testLoadUserByUsername() throws Exception {
        String[] roles;
        User expected;
        for (String user : testUserDetails.stringPropertyNames()) {
            expected = parseUserDetails(testUserDetails.get(user).toString());
            assertEquals(expected, userDetailsService.loadUserByUsername(user));
        }
    }

    private User parseUserDetails(final String s) {
        final String[] userRoles = s.split(":");
        final String[] roles = userRoles[1].split("\\|");
        final Collection<SimpleGrantedAuthority> roleAuthorities = new ArrayList<SimpleGrantedAuthority>(roles.length);
        for (String role : roles) {
            roleAuthorities.add(new SimpleGrantedAuthority(role));
        }
        return new User(userRoles[0], LdapUserDetailsService.UNKNOWN_PASSWORD, roleAuthorities);
    }
}

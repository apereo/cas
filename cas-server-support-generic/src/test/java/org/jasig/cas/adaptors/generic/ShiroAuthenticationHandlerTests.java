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

package org.jasig.cas.adaptors.generic;

import org.jasig.cas.authentication.RememberMeUsernamePasswordCredential;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.FailedLoginException;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link ShiroAuthenticationHandler}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class ShiroAuthenticationHandlerTests {

    @Test
    public void checkAuthenticationSuccessful() throws Exception {
        final ShiroAuthenticationHandler shiro = new ShiroAuthenticationHandler();
        shiro.setShiroConfiguration(new ClassPathResource("shiro.ini"));

        final RememberMeUsernamePasswordCredential creds =
                new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");

        assertNotNull(shiro.authenticate(creds));

    }

    @Test
    public void checkAuthenticationSuccessfulRolesAndPermissions() throws Exception {
        final ShiroAuthenticationHandler shiro = new ShiroAuthenticationHandler();
        shiro.setShiroConfiguration(new ClassPathResource("shiro.ini"));
        shiro.setRequiredRoles(Collections.singleton("admin"));
        shiro.setRequiredPermissions(Collections.singleton("superuser:deleteAll"));

        final RememberMeUsernamePasswordCredential creds =
                new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");

        assertNotNull(shiro.authenticate(creds));

    }

    @Test(expected=FailedLoginException.class)
    public void checkAuthenticationSuccessfulMissingRole() throws Exception {
        final ShiroAuthenticationHandler shiro = new ShiroAuthenticationHandler();
        shiro.setShiroConfiguration(new ClassPathResource("shiro.ini"));
        shiro.setRequiredRoles(Collections.singleton("student"));

        final RememberMeUsernamePasswordCredential creds =
                new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");
        shiro.authenticate(creds);
    }

    @Test(expected=FailedLoginException.class)
    public void checkAuthenticationSuccessfulMissingPermission() throws Exception {
        final ShiroAuthenticationHandler shiro = new ShiroAuthenticationHandler();
        shiro.setShiroConfiguration(new ClassPathResource("shiro.ini"));
        shiro.setRequiredPermissions(Collections.singleton("dosomething"));

        final RememberMeUsernamePasswordCredential creds =
                new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");
        shiro.authenticate(creds);
    }
}

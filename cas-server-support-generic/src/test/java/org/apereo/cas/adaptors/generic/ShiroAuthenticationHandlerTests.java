package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.RememberMeUsernamePasswordCredential;

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
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

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
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));
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
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));
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
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));
        shiro.setRequiredPermissions(Collections.singleton("dosomething"));

        final RememberMeUsernamePasswordCredential creds =
                new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");
        shiro.authenticate(creds);
    }
}

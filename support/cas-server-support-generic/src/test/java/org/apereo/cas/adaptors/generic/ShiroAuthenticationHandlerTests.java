package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.RememberMeUsernamePasswordCredential;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void checkAuthenticationSuccessful() throws Exception {
        final ShiroAuthenticationHandler shiro = new ShiroAuthenticationHandler();
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

        final RememberMeUsernamePasswordCredential creds = new RememberMeUsernamePasswordCredential();
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

        final RememberMeUsernamePasswordCredential creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");

        assertNotNull(shiro.authenticate(creds));
    }

    @Test
    public void checkAuthenticationSuccessfulMissingRole() throws Exception {
        final ShiroAuthenticationHandler shiro = new ShiroAuthenticationHandler();
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));
        shiro.setRequiredRoles(Collections.singleton("student"));

        final RememberMeUsernamePasswordCredential creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");

        this.thrown.expect(FailedLoginException.class);
        this.thrown.expectMessage("Required role student does not exist");

        shiro.authenticate(creds);
    }

    @Test
    public void checkAuthenticationSuccessfulMissingPermission() throws Exception {
        final ShiroAuthenticationHandler shiro = new ShiroAuthenticationHandler();
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));
        shiro.setRequiredPermissions(Collections.singleton("dosomething"));

        final RememberMeUsernamePasswordCredential creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");

        this.thrown.expect(FailedLoginException.class);
        this.thrown.expectMessage("Required permission dosomething cannot be located");

        shiro.authenticate(creds);
    }
}

package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.RememberMeUsernamePasswordCredential;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.FailedLoginException;
import java.util.Collections;
import java.util.HashSet;

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
        final ShiroAuthenticationHandler shiro = new ShiroAuthenticationHandler("", null, null, new HashSet<>(0), new HashSet<>(0));
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

        final RememberMeUsernamePasswordCredential creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");

        assertNotNull(shiro.authenticate(creds));
    }

    @Test
    public void checkAuthenticationSuccessfulRolesAndPermissions() throws Exception {
        final ShiroAuthenticationHandler shiro = new ShiroAuthenticationHandler("", null, null, Collections.singleton("admin"),
                Collections.singleton("superuser:deleteAll"));
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

        final RememberMeUsernamePasswordCredential creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");

        assertNotNull(shiro.authenticate(creds));
    }

    @Test
    public void checkAuthenticationSuccessfulMissingRole() throws Exception {
        final ShiroAuthenticationHandler shiro = new ShiroAuthenticationHandler("", null, null, Collections.singleton("student"), new HashSet<>(0));
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

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
        final ShiroAuthenticationHandler shiro = new ShiroAuthenticationHandler("", null, null, new HashSet<>(0), Collections.singleton("dosomething"));
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

        final RememberMeUsernamePasswordCredential creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");

        this.thrown.expect(FailedLoginException.class);
        this.thrown.expectMessage("Required permission dosomething cannot be located");

        shiro.authenticate(creds);
    }
}

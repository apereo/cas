package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.FailedLoginException;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Handles tests for {@link ShiroAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class ShiroAuthenticationHandlerTests {

    @Test
    public void checkAuthenticationSuccessful() throws Exception {
        val shiro = new ShiroAuthenticationHandler("", null, null, new HashSet<>(0), new HashSet<>(0));
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

        val creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");

        assertNotNull(shiro.authenticate(creds));
    }

    @Test
    public void checkAuthenticationSuccessfulRolesAndPermissions() throws Exception {
        val shiro = new ShiroAuthenticationHandler("", null, null, Collections.singleton("admin"),
            Collections.singleton("superuser:deleteAll"));
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

        val creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");

        assertNotNull(shiro.authenticate(creds));
    }

    @Test
    public void checkAuthenticationSuccessfulMissingRole() {
        val shiro = new ShiroAuthenticationHandler("", null, null, Collections.singleton("student"), new HashSet<>(0));
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

        val creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");

        assertThrows(FailedLoginException.class, () -> shiro.authenticate(creds));
    }

    @Test
    public void checkAuthenticationSuccessfulMissingPermission() {
        val shiro = new ShiroAuthenticationHandler("", null, null, new HashSet<>(0), Collections.singleton("dosomething"));
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

        val creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.setPassword("Mellon");

        assertThrows(FailedLoginException.class, () -> shiro.authenticate(creds));
    }
}

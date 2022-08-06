package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Handles tests for {@link ShiroAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 * @deprecated This component is deprecated as of 6.6.0 and is scheduled to be removed.
 */
@Tag("AuthenticationHandler")
@Deprecated(since = "6.6.0")
public class ShiroAuthenticationHandlerTests {

    @SneakyThrows
    private static ShiroAuthenticationHandler buildShiroHandlerWithAccountStatus(final Class<? extends ShiroException> clazz) {
        val shiro = new ShiroAuthenticationHandler(StringUtils.EMPTY, null,
            PrincipalFactoryUtils.newPrincipalFactory(), Collections.singleton("admin"),
            Collections.singleton("superuser:deleteAll"));
        val ex = clazz.getDeclaredConstructor().newInstance();
        shiro.setPasswordPolicyHandlingStrategy((o, o2) -> {
            throw ex;
        });
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));
        return shiro;
    }

    @Test
    public void checkAuthenticationSuccessful() throws Exception {
        val shiro = new ShiroAuthenticationHandler(StringUtils.EMPTY, null,
            PrincipalFactoryUtils.newPrincipalFactory(), new HashSet<>(0), new HashSet<>(0));
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

        val creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.assignPassword("Mellon");

        assertNotNull(shiro.authenticate(creds, mock(Service.class)));
    }

    @Test
    public void checkAuthenticationSuccessfulRolesAndPermissions() throws Exception {
        val shiro = new ShiroAuthenticationHandler(StringUtils.EMPTY, null,
            PrincipalFactoryUtils.newPrincipalFactory(), Collections.singleton("admin"),
            Collections.singleton("superuser:deleteAll"));
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

        val creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.assignPassword("Mellon");

        assertNotNull(shiro.authenticate(creds, mock(Service.class)));
    }

    @Test
    public void checkAuthenticationSuccessfulMissingRole() {
        val shiro = new ShiroAuthenticationHandler(StringUtils.EMPTY, null,
            PrincipalFactoryUtils.newPrincipalFactory(), Collections.singleton("student"), new HashSet<>(0));
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

        val creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.assignPassword("Mellon");

        assertThrows(FailedLoginException.class, () -> shiro.authenticate(creds, mock(Service.class)));
    }

    @Test
    public void checkAuthenticationSuccessfulMissingPermission() {
        val shiro = new ShiroAuthenticationHandler(StringUtils.EMPTY, null,
            PrincipalFactoryUtils.newPrincipalFactory(), new HashSet<>(0), Collections.singleton("dosomething"));
        shiro.loadShiroConfiguration(new ClassPathResource("shiro.ini"));

        val creds = new RememberMeUsernamePasswordCredential();
        creds.setRememberMe(true);
        creds.setUsername("casuser");
        creds.assignPassword("Mellon");

        assertThrows(FailedLoginException.class, () -> shiro.authenticate(creds, mock(Service.class)));
    }

    @Test
    public void checkAuthenticationAccountStatusHandling() {
        val creds = new RememberMeUsernamePasswordCredential();
        creds.setUsername("casuser");
        creds.assignPassword("Mellon");

        assertThrows(AccountNotFoundException.class,
            () -> buildShiroHandlerWithAccountStatus(UnknownAccountException.class).authenticate(creds, mock(Service.class)));

        assertThrows(AccountLockedException.class,
            () -> buildShiroHandlerWithAccountStatus(LockedAccountException.class).authenticate(creds, mock(Service.class)));

        assertThrows(CredentialExpiredException.class,
            () -> buildShiroHandlerWithAccountStatus(ExpiredCredentialsException.class).authenticate(creds, mock(Service.class)));

        assertThrows(AccountDisabledException.class,
            () -> buildShiroHandlerWithAccountStatus(DisabledAccountException.class).authenticate(creds, mock(Service.class)));

        assertThrows(FailedLoginException.class,
            () -> buildShiroHandlerWithAccountStatus(AuthenticationException.class).authenticate(creds, mock(Service.class)));
    }
}

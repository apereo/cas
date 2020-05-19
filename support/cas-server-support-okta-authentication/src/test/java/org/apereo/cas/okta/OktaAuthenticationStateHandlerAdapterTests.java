package org.apereo.cas.okta;

import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.support.password.DefaultPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;

import com.okta.authn.sdk.resource.AuthenticationResponse;
import com.okta.authn.sdk.resource.User;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OktaAuthenticationStateHandlerAdapterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class OktaAuthenticationStateHandlerAdapterTests {
    @Test
    public void handleSuccessWithoutToken() {
        val adapter = new OktaAuthenticationStateHandlerAdapter(
            new DefaultPasswordPolicyHandlingStrategy<>(), new PasswordPolicyContext());
        val response = mock(AuthenticationResponse.class);
        when(response.getSessionToken()).thenReturn(null);
        adapter.handleSuccess(response);
        assertThrows(FailedLoginException.class, adapter::throwExceptionIfNecessary);
    }

    @Test
    public void handleSuccess() {
        val adapter = new OktaAuthenticationStateHandlerAdapter(
            new DefaultPasswordPolicyHandlingStrategy<>(), new PasswordPolicyContext());
        val response = mock(AuthenticationResponse.class);
        when(response.getSessionToken()).thenReturn("token");
        when(response.getStatusString()).thenReturn("error");
        val user = mock(User.class);
        when(user.getLogin()).thenReturn("cas");
        when(user.getId()).thenReturn("cas-id");
        when(user.getProfile()).thenReturn(Map.of("name", "something", "lastName", "something-else"));
        when(response.getUser()).thenReturn(user);
        adapter.handleSuccess(response);
        assertDoesNotThrow(adapter::throwExceptionIfNecessary);
        assertEquals("cas", adapter.getUsername());
        assertFalse(adapter.getUserAttributes().isEmpty());
    }

    @Test
    public void handlePasswordWarning() {
        val adapter = new OktaAuthenticationStateHandlerAdapter(
            new DefaultPasswordPolicyHandlingStrategy<>(), new PasswordPolicyContext());
        val response = mock(AuthenticationResponse.class);
        when(response.getSessionToken()).thenReturn("token");
        adapter.handlePasswordWarning(response);
        assertThrows(AccountNotFoundException.class, adapter::throwExceptionIfNecessary);
        assertTrue(adapter.getWarnings().isEmpty());
    }

    @Test
    public void verifyLockout() {
        val adapter = new OktaAuthenticationStateHandlerAdapter(
            new DefaultPasswordPolicyHandlingStrategy<>(), new PasswordPolicyContext());
        val response = mock(AuthenticationResponse.class);
        when(response.getStatusString()).thenReturn("error");
        adapter.handleLockedOut(response);
        assertThrows(AccountLockedException.class, adapter::throwExceptionIfNecessary);
    }

    @Test
    public void verifyUnknown() {
        val adapter = new OktaAuthenticationStateHandlerAdapter(
            new DefaultPasswordPolicyHandlingStrategy<>(), new PasswordPolicyContext());
        val response = mock(AuthenticationResponse.class);
        when(response.getStatusString()).thenReturn("error");
        adapter.handleUnknown(response);
        assertThrows(AccountNotFoundException.class, adapter::throwExceptionIfNecessary);
    }

    @Test
    public void handleUnauthenticated() {
        val adapter = new OktaAuthenticationStateHandlerAdapter(
            new DefaultPasswordPolicyHandlingStrategy<>(), new PasswordPolicyContext());
        val response = mock(AuthenticationResponse.class);
        when(response.getStatusString()).thenReturn("error");
        adapter.handleUnauthenticated(response);
        assertThrows(FailedLoginException.class, adapter::throwExceptionIfNecessary);
    }

    @Test
    public void handlePasswordExpired() {
        val adapter = new OktaAuthenticationStateHandlerAdapter(
            new DefaultPasswordPolicyHandlingStrategy<>(), new PasswordPolicyContext());
        val response = mock(AuthenticationResponse.class);
        when(response.getStatusString()).thenReturn("error");
        adapter.handlePasswordExpired(response);
        assertThrows(AccountExpiredException.class, adapter::throwExceptionIfNecessary);
    }

    @Test
    public void handlePasswordReset() {
        val adapter = new OktaAuthenticationStateHandlerAdapter(
            new DefaultPasswordPolicyHandlingStrategy<>(), new PasswordPolicyContext());
        val response = mock(AuthenticationResponse.class);
        when(response.getStatusString()).thenReturn("error");
        adapter.handlePasswordReset(response);
        assertThrows(AccountPasswordMustChangeException.class, adapter::throwExceptionIfNecessary);
    }
}

package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultLdapAccountStateHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Ldap")
public class DefaultLdapAccountStateHandlerTests {
    @Test
    public void verifyActiveDirectoryErrors() {
        val handler = new DefaultLdapAccountStateHandler();

        val response = mock(AuthenticationResponse.class);
        when(response.isSuccess()).thenReturn(false);
        when(response.getDiagnosticMessage()).thenReturn("error data 533");
        assertThrows(AccountDisabledException.class, () -> handler.handle(response, new PasswordPolicyContext()));
        when(response.getDiagnosticMessage()).thenReturn("error data 532");
        assertThrows(CredentialExpiredException.class, () -> handler.handle(response, new PasswordPolicyContext()));
        when(response.getDiagnosticMessage()).thenReturn("error data 530");
        assertThrows(InvalidLoginTimeException.class, () -> handler.handle(response, new PasswordPolicyContext()));
        when(response.getDiagnosticMessage()).thenReturn("error data 701");
        assertThrows(AccountExpiredException.class, () -> handler.handle(response, new PasswordPolicyContext()));
        when(response.getDiagnosticMessage()).thenReturn("error data 773");
        assertThrows(AccountPasswordMustChangeException.class, () -> handler.handle(response, new PasswordPolicyContext()));
        when(response.getDiagnosticMessage()).thenReturn("error data 775");
        assertThrows(AccountLockedException.class, () -> handler.handle(response, new PasswordPolicyContext()));

        when(response.getDiagnosticMessage()).thenReturn("error unknown");
        assertDoesNotThrow(() -> {
            handler.handle(response, new PasswordPolicyContext());
        });
    }

    @Test
    public void verifyOperation() {
        val handler = new DefaultLdapAccountStateHandler();
        handler.setAttributesToErrorMap(Map.of("attr1", AccountLockedException.class));

        val response = mock(AuthenticationResponse.class);
        val entry = new LdapEntry();
        entry.addAttributes(new LdapAttribute("attr1", "true"));
        when(response.getLdapEntry()).thenReturn(entry);

        assertThrows(AccountLockedException.class, () ->
            handler.handlePolicyAttributes(response));
    }

    @Test
    public void verifyNoAttrs() {
        val handler = new DefaultLdapAccountStateHandler();
        val response = mock(AuthenticationResponse.class);
        handler.setAttributesToErrorMap(Map.of("attr1", AccountLockedException.class));
        val entry = new LdapEntry();
        when(response.getLdapEntry()).thenReturn(entry);
        when(response.isSuccess()).thenReturn(Boolean.TRUE);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                handler.handle(response, new PasswordPolicyContext());
            }
        });
    }

    @Test
    public void verifyNoWarning() {
        val handler = new DefaultLdapAccountStateHandler();
        val response = mock(AuthenticationResponse.class);
        handler.setAttributesToErrorMap(Map.of("attr1", AccountLockedException.class));
        val entry = new LdapEntry();
        val accountState = mock(AccountState.class);
        when(response.getAccountState()).thenReturn(accountState);
        when(response.getLdapEntry()).thenReturn(entry);
        when(response.isSuccess()).thenReturn(Boolean.TRUE);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                handler.handle(response, new PasswordPolicyContext());
            }
        });

        val warning = mock(AccountState.Warning.class);
        when(accountState.getWarning()).thenReturn(warning);
        when(response.getAccountState()).thenReturn(accountState);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                handler.handle(response, new PasswordPolicyContext());
            }
        });
    }

}

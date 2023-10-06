package org.apereo.cas.authentication.handler.support.jaas;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;

import java.util.Map;
import java.util.Set;

import static org.apereo.cas.util.junit.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccountsPreDefinedLoginModuleTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class AccountsPreDefinedLoginModuleTests {

    @Test
    void verifyOperation() throws Throwable {
        val module = new AccountsPreDefinedLoginModule();
        assertThrowsWithRootCause(IllegalArgumentException.class, FailedLoginException.class, module::login);
        assertFalse(module.abort());
        module.initialize(new Subject(true, Set.of(new AccountsPreDefinedLoginModule.StaticPrincipal()),
            Set.of(), Set.of()), mock(CallbackHandler.class), Map.of(), Map.of());
        assertFalse(module.login());
    }
}

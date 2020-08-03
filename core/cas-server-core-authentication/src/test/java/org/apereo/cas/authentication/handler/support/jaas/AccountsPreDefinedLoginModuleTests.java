package org.apereo.cas.authentication.handler.support.jaas;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccountsPreDefinedLoginModuleTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
public class AccountsPreDefinedLoginModuleTests {

    @Test
    public void verifyOperation() throws Exception {
        val module = new AccountsPreDefinedLoginModule();
        assertThrows(FailedLoginException.class, module::login);
        assertFalse(module.abort());
        module.initialize(new Subject(true, Set.of(new AccountsPreDefinedLoginModule.StaticPrincipal()),
            Set.of(), Set.of()), mock(CallbackHandler.class), Map.of(), Map.of());
        assertFalse(module.login());
    }

}

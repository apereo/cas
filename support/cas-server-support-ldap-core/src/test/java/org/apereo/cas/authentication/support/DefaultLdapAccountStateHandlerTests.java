package org.apereo.cas.authentication.support;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.auth.AuthenticationResponse;

import javax.security.auth.login.AccountLockedException;

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
}

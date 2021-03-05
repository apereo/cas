package org.apereo.cas.authentication.support;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResultCode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RejectResultCodeLdapPasswordPolicyHandlingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Ldap")
public class RejectResultCodeLdapPasswordPolicyHandlingStrategyTests {
    @Test
    public void verifyStrategySupportsDefault() {
        val s = new RejectResultCodeLdapPasswordPolicyHandlingStrategy();
        val res = mock(AuthenticationResponse.class);
        when(res.getAuthenticationResultCode()).thenReturn(AuthenticationResultCode.INVALID_CREDENTIAL);
        assertFalse(s.supports(null));

        when(res.isSuccess()).thenReturn(false);
        assertFalse(s.supports(res));

        when(res.isSuccess()).thenReturn(true);
        assertFalse(s.supports(res));
    }
}

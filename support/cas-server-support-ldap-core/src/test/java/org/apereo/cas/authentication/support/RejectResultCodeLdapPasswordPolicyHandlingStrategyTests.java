package org.apereo.cas.authentication.support;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResultCode;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RejectResultCodeLdapPasswordPolicyHandlingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
public class RejectResultCodeLdapPasswordPolicyHandlingStrategyTests {

    @Test
    public void verifyStrategySupportsDefault() {
        final RejectResultCodeLdapPasswordPolicyHandlingStrategy s = new RejectResultCodeLdapPasswordPolicyHandlingStrategy();
        final AuthenticationResponse res = mock(AuthenticationResponse.class);
        when(res.getAuthenticationResultCode()).thenReturn(AuthenticationResultCode.INVALID_CREDENTIAL);
        assertFalse(s.supports(null));

        when(res.getResult()).thenReturn(false);
        assertFalse(s.supports(res));
        
        when(res.getResult()).thenReturn(true);
        assertFalse(s.supports(res));
    }
}

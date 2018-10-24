package org.apereo.cas.authentication.support;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResultCode;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RejectResultCodeLdapPasswordPolicyHandlingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RejectResultCodeLdapPasswordPolicyHandlingStrategyTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Test
    public void verifyStrategySupportsDefault() {
        val s = new RejectResultCodeLdapPasswordPolicyHandlingStrategy();
        val res = mock(AuthenticationResponse.class);
        when(res.getAuthenticationResultCode()).thenReturn(AuthenticationResultCode.INVALID_CREDENTIAL);
        assertFalse(s.supports(null));

        when(res.getResult()).thenReturn(false);
        assertFalse(s.supports(res));

        when(res.getResult()).thenReturn(true);
        assertFalse(s.supports(res));
    }
}

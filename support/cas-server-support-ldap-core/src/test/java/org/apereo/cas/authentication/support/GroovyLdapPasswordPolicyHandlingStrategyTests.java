package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.MessageDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResultCode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyLdapPasswordPolicyHandlingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
public class GroovyLdapPasswordPolicyHandlingStrategyTests {

    @Test
    public void verifyStrategySupportsDefault() {
        final ClassPathResource resource = new ClassPathResource("lppe-strategy.groovy");
        final GroovyLdapPasswordPolicyHandlingStrategy s = new GroovyLdapPasswordPolicyHandlingStrategy(resource);
        final AuthenticationResponse res = mock(AuthenticationResponse.class);

        when(res.getAuthenticationResultCode()).thenReturn(AuthenticationResultCode.INVALID_CREDENTIAL);
        assertFalse(s.supports(null));

        when(res.getResult()).thenReturn(false);
        assertTrue(s.supports(res));

        final List<MessageDescriptor> results = s.handle(res, mock(LdapPasswordPolicyConfiguration.class));
        assertFalse(results.isEmpty());
    }
}

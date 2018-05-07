package org.apereo.cas.authentication.support;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.support.password.GroovyPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
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
 * This is {@link GroovyPasswordPolicyHandlingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@Slf4j
public class GroovyPasswordPolicyHandlingStrategyTests {

    @Test
    public void verifyStrategySupportsDefault() {
        final var resource = new ClassPathResource("lppe-strategy.groovy");
        final var s = new GroovyPasswordPolicyHandlingStrategy(resource);
        final var res = mock(AuthenticationResponse.class);

        when(res.getAuthenticationResultCode()).thenReturn(AuthenticationResultCode.INVALID_CREDENTIAL);
        assertFalse(s.supports(null));

        when(res.getResult()).thenReturn(false);
        assertTrue(s.supports(res));

        final List<MessageDescriptor> results = s.handle(res, mock(PasswordPolicyConfiguration.class));
        assertFalse(results.isEmpty());
    }
}

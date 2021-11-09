package org.apereo.cas.acct.provision;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccountRegistrationProvisionerConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Simple")
public class AccountRegistrationProvisionerConfigurerTests {
    @Test
    public void verifyOperation() {
        val configurer = mock(AccountRegistrationProvisionerConfigurer.class);
        when(configurer.getOrder()).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, configurer.getOrder());
    }
}

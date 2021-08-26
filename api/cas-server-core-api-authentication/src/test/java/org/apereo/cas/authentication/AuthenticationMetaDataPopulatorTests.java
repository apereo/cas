package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationMetadata")
public class AuthenticationMetaDataPopulatorTests {

    @Test
    public void verifyOperation() {
        val policy = mock(AuthenticationMetaDataPopulator.class);
        when(policy.getOrder()).thenCallRealMethod();
        assertEquals(Ordered.HIGHEST_PRECEDENCE, policy.getOrder());
    }

}

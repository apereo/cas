package org.apereo.cas.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServicePrincipalAttributesRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
public class RegisteredServicePrincipalAttributesRepositoryTests {
    @Test
    public void verifyOperation() {
        val policy = mock(RegisteredServicePrincipalAttributesRepository.class);
        when(policy.getAttributeRepositoryIds()).thenCallRealMethod();
        assertTrue(policy.getAttributeRepositoryIds().isEmpty());
    }
}

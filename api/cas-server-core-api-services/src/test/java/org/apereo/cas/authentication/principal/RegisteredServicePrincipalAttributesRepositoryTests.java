package org.apereo.cas.authentication.principal;

import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

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
        doCallRealMethod().when(policy).update(any(), any(), any());
        assertDoesNotThrow(() -> policy.update(UUID.randomUUID().toString(), Map.of(), mock(RegisteredService.class)));
    }
}

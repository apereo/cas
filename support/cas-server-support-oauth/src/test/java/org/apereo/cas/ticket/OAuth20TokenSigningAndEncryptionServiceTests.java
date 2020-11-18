package org.apereo.cas.ticket;

import org.apereo.cas.AbstractOAuth20Tests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OAuth20TokenSigningAndEncryptionServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20TokenSigningAndEncryptionServiceTests extends AbstractOAuth20Tests {
    @Test
    public void verifyOperation() {
        val service = mock(OAuth20TokenSigningAndEncryptionService.class);
        when(service.shouldEncryptToken(any())).thenCallRealMethod();
        when(service.shouldSignToken(any())).thenCallRealMethod();
        assertFalse(service.shouldEncryptToken(getRegisteredService("clientid", "secret")));
        assertFalse(service.shouldSignToken(getRegisteredService("clientid", "secret")));
    }

}

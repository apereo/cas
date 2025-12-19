package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import lombok.val;
import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link TokenSigningAndEncryptionServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OAuth")
class TokenSigningAndEncryptionServiceTests extends AbstractOAuth20Tests {
    @Test
    void verifyOperation() throws Throwable {
        val publicKey = mock(PublicJsonWebKey.class);
        when(publicKey.getPublicKey()).thenReturn(null);

        val service = mock(BaseTokenSigningAndEncryptionService.class);
        when(service.decode(anyString(), any())).thenCallRealMethod();
        when(service.getJsonWebKeySigningKey(Optional.empty())).thenReturn(publicKey);

        assertThrows(IllegalArgumentException.class,
            () -> service.decode(UUID.randomUUID().toString(), Optional.empty()));
    }

    @Test
    void verifyBadSignatureOperation() throws Throwable {
        val publicKey = mock(PublicJsonWebKey.class);
        when(publicKey.getPublicKey()).thenReturn(mock(PublicKey.class));

        val service = mock(BaseTokenSigningAndEncryptionService.class);
        when(service.getJsonWebKeySigningKey(Optional.empty())).thenReturn(publicKey);
        when(service.decode(anyString(), any())).thenCallRealMethod();
        when(service.verifySignature(anyString(), any())).thenReturn(null);

        assertThrows(NullPointerException.class,
            () -> service.decode(UUID.randomUUID().toString(), Optional.empty()));
    }

}

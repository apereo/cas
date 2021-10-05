package org.apereo.cas.adaptors.radius.authentication;

import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.BaseAbstractMultifactorAuthenticationProviderTests;
import org.apereo.cas.authentication.mfa.MultifactorAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import net.jradius.exception.TimeoutException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RadiusMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Radius")
public class RadiusMultifactorAuthenticationProviderTests extends BaseAbstractMultifactorAuthenticationProviderTests {
    @Override
    public AbstractMultifactorAuthenticationProvider getMultifactorAuthenticationProvider() {
        return new RadiusMultifactorAuthenticationProvider(CollectionUtils.wrapList(mock(RadiusServer.class)));
    }

    @Test
    public void verifyPingFails() throws Exception {
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        val server = mock(RadiusServer.class);
        when(server.authenticate(anyString(), anyString())).thenThrow(new TimeoutException("timeout"));
        val p = new RadiusMultifactorAuthenticationProvider(CollectionUtils.wrapList(server));
        assertDoesNotThrow(() -> p.isAvailable(service));
    }

    @Test
    public void verifyPingPasses() throws Exception {
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        val server = mock(RadiusServer.class);
        when(server.authenticate(anyString(), anyString())).thenThrow(new RuntimeException("pass"));
        val p = new RadiusMultifactorAuthenticationProvider(CollectionUtils.wrapList(server));
        assertDoesNotThrow(() -> p.isAvailable(service));
    }
}

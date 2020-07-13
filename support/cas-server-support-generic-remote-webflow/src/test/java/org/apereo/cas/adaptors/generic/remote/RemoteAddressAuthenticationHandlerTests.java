package org.apereo.cas.adaptors.generic.remote;

import org.apereo.cas.BaseRemoteAddressTests;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RemoteAddressAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseRemoteAddressTests.SharedTestConfiguration.class,
    properties = "cas.authn.remote-address.ip-address-range=192.168.1.0/255.255.255.0")
@Tag("Authentication")
public class RemoteAddressAuthenticationHandlerTests {
    @Autowired
    @Qualifier("remoteAddressAuthenticationHandler")
    private AuthenticationHandler remoteAddressAuthenticationHandler;

    @Test
    @SneakyThrows
    public void verifyAccount() {
        val c = new RemoteAddressCredential("192.168.1.7");
        val result = remoteAddressAuthenticationHandler.authenticate(c);
        assertNotNull(result);
        assertEquals(c.getId(), result.getPrincipal().getId());
    }

    @Test
    public void verifySupports() {
        val c = new RemoteAddressCredential("172.217.12.206");
        assertTrue(remoteAddressAuthenticationHandler.supports(c));
        assertFalse(remoteAddressAuthenticationHandler.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }
}

package org.apereo.cas.adaptors.generic.remote;

import org.apereo.cas.BaseRemoteAddressTests;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RemoteAddressAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseRemoteAddressTests.SharedTestConfiguration.class,
    properties = "cas.authn.remote.ip-address-range=192.168.1.0/255.255.255.0")
@Tag("AuthenticationHandler")
class RemoteAddressAuthenticationHandlerTests {
    @Autowired
    @Qualifier("remoteAddressAuthenticationHandler")
    private AuthenticationHandler remoteAddressAuthenticationHandler;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyAccount() throws Throwable {
        val credential = new RemoteAuthenticationCredential("192.168.1.7");
        val result = remoteAddressAuthenticationHandler.authenticate(credential, mock(Service.class));
        assertNotNull(result);
        assertEquals(credential.getId(), result.getPrincipal().getId());
    }

    @Test
    void verifyAccountFails() throws Throwable {
        val credential = new RemoteAuthenticationCredential("---");
        assertThrows(FailedLoginException.class, () -> remoteAddressAuthenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyBadRange() throws Throwable {
        val credential = new RemoteAuthenticationCredential("---");
        val handler = new RemoteAddressAuthenticationHandler(casProperties.getAuthn().getRemote(),
            servicesManager, PrincipalFactoryUtils.newPrincipalFactory());
        handler.configureIpNetworkRange("abc/def");
        assertThrows(FailedLoginException.class, () -> remoteAddressAuthenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifySupports() throws Throwable {
        val credential = new RemoteAuthenticationCredential("172.217.12.206");
        assertTrue(remoteAddressAuthenticationHandler.supports(credential));
        assertTrue(remoteAddressAuthenticationHandler.supports(credential.getClass()));
        assertFalse(remoteAddressAuthenticationHandler.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));
    }
}

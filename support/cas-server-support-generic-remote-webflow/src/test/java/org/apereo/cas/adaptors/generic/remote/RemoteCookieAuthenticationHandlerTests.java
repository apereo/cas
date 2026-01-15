package org.apereo.cas.adaptors.generic.remote;

import module java.base;
import org.apereo.cas.BaseRemoteAddressTests;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.PropertyBoundCipherExecutor;
import lombok.val;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RemoteCookieAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = BaseRemoteAddressTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.remote.cookie.cookie-name=MyCookie",
        "cas.authn.remote.cookie.crypto.alg=" + ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256,
        "cas.authn.remote.cookie.crypto.encryption.key=u696jJnPvm1DHLR7yVCSKMMzzoPoFxJZW4-MP1CkM5w",
        "cas.authn.remote.cookie.crypto.signing.key=zPdNCd0R1oMR0ClzEqZzapkte8rO0tNvygYjmHoUhitAu6CBscwMC3ZTKy8tleTKiQ6GVcuiQQgxfd1nSKxf7w"
    })
@Tag("AuthenticationHandler")
@ExtendWith(CasTestExtension.class)
class RemoteCookieAuthenticationHandlerTests {
    @Autowired
    @Qualifier("remoteCookieAuthenticationHandler")
    private AuthenticationHandler remoteCookieAuthenticationHandler;

    @Autowired
    @Qualifier("remoteCookieCipherExecutor")
    private PropertyBoundCipherExecutor remoteCookieCipherExecutor;

    @Test
    void verifyAccount() throws Throwable {
        val cookieValue = remoteCookieCipherExecutor.encode("casuser");
        val credential = new RemoteAuthenticationCredential(null, cookieValue.toString());
        assertTrue(remoteCookieAuthenticationHandler.supports(credential));
        assertTrue(remoteCookieAuthenticationHandler.supports(credential.getClass()));
        val result = remoteCookieAuthenticationHandler.authenticate(credential, mock(Service.class));
        assertNotNull(result);
        assertEquals("casuser", result.getPrincipal().getId());
        assertNotNull(remoteCookieCipherExecutor.getSigningKeySetting());
        assertNotNull(remoteCookieCipherExecutor.getEncryptionKeySetting());
    }

    @Test
    void verifyFailsAuthn() {
        val credential = new RemoteAuthenticationCredential(null, UUID.randomUUID().toString());
        assertThrows(FailedLoginException.class,
            () -> remoteCookieAuthenticationHandler.authenticate(credential, mock(Service.class)));
    }

}

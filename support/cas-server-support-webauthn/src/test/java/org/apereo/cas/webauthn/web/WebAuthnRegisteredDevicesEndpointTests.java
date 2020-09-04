package org.apereo.cas.webauthn.web;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import org.apereo.cas.webauthn.web.flow.BaseWebAuthnWebflowTests;

import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.CredentialRegistration;
import com.yubico.webauthn.data.exception.Base64UrlException;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnRegisteredDevicesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = {
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.webAuthnDevices.enabled=true"
    })
@Tag("MFA")
public class WebAuthnRegisteredDevicesEndpointTests {
    @Autowired
    @Qualifier("webAuthnRegisteredDevicesEndpoint")
    private WebAuthnRegisteredDevicesEndpoint webAuthnRegisteredDevicesEndpoint;

    @Autowired
    @Qualifier("webAuthnCredentialRepository")
    private WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Test
    public void verifyOperation() throws Exception {
        val id1 = UUID.randomUUID().toString();
        register(RegisteredServiceTestUtils.getAuthentication(id1));

        val id2 = UUID.randomUUID().toString();
        register(RegisteredServiceTestUtils.getAuthentication(id2));

        assertFalse(webAuthnRegisteredDevicesEndpoint.fetch(id1).isEmpty());
        assertFalse(webAuthnRegisteredDevicesEndpoint.fetch(id2).isEmpty());

        webAuthnRegisteredDevicesEndpoint.delete(id1, id1);
        assertTrue(webAuthnRegisteredDevicesEndpoint.fetch(id1).isEmpty());
        
        webAuthnRegisteredDevicesEndpoint.delete(id2);
        assertTrue(webAuthnRegisteredDevicesEndpoint.fetch(id1).isEmpty());
        assertTrue(webAuthnRegisteredDevicesEndpoint.fetch(id2).isEmpty());
    }

    private void register(final Authentication authn) throws Base64UrlException {
        webAuthnCredentialRepository.addRegistrationByUsername(authn.getPrincipal().getId(),
            CredentialRegistration.builder()
                .credential(RegisteredCredential.builder()
                    .credentialId(ByteArray.fromBase64Url(authn.getPrincipal().getId()))
                    .userHandle(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .publicKeyCose(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .build())
                .build());
    }


}

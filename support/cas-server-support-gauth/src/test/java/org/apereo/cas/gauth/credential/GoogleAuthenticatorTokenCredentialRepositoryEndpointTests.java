package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorTokenCredentialRepositoryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import(BaseGoogleAuthenticatorTests.SharedTestConfiguration.class)
@TestPropertySource(properties = "management.endpoint.gauthCredentialRepository.access=UNRESTRICTED")
@Getter
@Tag("MFAProvider")
class GoogleAuthenticatorTokenCredentialRepositoryEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("googleAuthenticatorTokenCredentialRepositoryEndpoint")
    private GoogleAuthenticatorTokenCredentialRepositoryEndpoint endpoint;

    @Autowired
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository registry;

    @Autowired
    @Qualifier("googleAuthenticatorMultifactorAuthenticationProvider")
    private MultifactorAuthenticationProvider googleAuthenticatorMultifactorAuthenticationProvider;

    @Test
    void verifyDeviceManager() {
        val acct = registry.create(UUID.randomUUID().toString());
        val toSave = GoogleAuthenticatorAccount.builder()
            .username(acct.getUsername())
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes())
            .name(UUID.randomUUID().toString())
            .build();
        registry.save(toSave);
        val principal = RegisteredServiceTestUtils.getPrincipal(acct.getUsername());
        val devices = googleAuthenticatorMultifactorAuthenticationProvider.getDeviceManager().findRegisteredDevices(principal);
        assertEquals(1, devices.size());
        assertTrue(googleAuthenticatorMultifactorAuthenticationProvider.getDeviceManager().hasRegisteredDevices(principal));
        val device = devices.getFirst();
        googleAuthenticatorMultifactorAuthenticationProvider.getDeviceManager().removeRegisteredDevice(principal, device.getId());
        assertFalse(googleAuthenticatorMultifactorAuthenticationProvider.getDeviceManager().hasRegisteredDevices(principal));
    }
    
    @Test
    void verifyOperation() {
        val acct = registry.create(UUID.randomUUID().toString());
        val toSave = GoogleAuthenticatorAccount.builder()
            .username(acct.getUsername())
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes())
            .name(UUID.randomUUID().toString())
            .build();
        registry.save(toSave);
        assertNotNull(endpoint.get(acct.getUsername()));
        assertFalse(endpoint.load().isEmpty());

        val entity = endpoint.exportAccounts();
        assertEquals(HttpStatus.OK, entity.getStatusCode());

        endpoint.delete(acct.getUsername());
        assertTrue(endpoint.get(acct.getUsername()).isEmpty());
        endpoint.deleteAll();
        assertTrue(endpoint.load().isEmpty());
    }

    @Test
    void verifyImportOperation() throws Throwable {
        val acct = registry.create(UUID.randomUUID().toString());
        val toSave = GoogleAuthenticatorAccount.builder()
            .username(acct.getUsername())
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes())
            .name(UUID.randomUUID().toString())
            .build();
        val request = new MockHttpServletRequest();
        val content = new GoogleAuthenticatorAccountSerializer(applicationContext).toString(toSave);
        request.setContent(content.getBytes(StandardCharsets.UTF_8));
        assertEquals(HttpStatus.CREATED, endpoint.importAccount(request).getStatusCode());
    }
}

package org.apereo.cas.adaptors.duo.rest;

import module java.base;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityPasscodeCredential;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityRestHttpRequestCredentialFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("DuoSecurity")
class DuoSecurityRestHttpRequestCredentialFactoryTests {
    @Test
    void verifyOperation() throws Throwable {
        val factory = new DuoSecurityRestHttpRequestCredentialFactory(mock(TenantExtractor.class));
        val request = new MockHttpServletRequest();

        val body = new LinkedMultiValueMap<String, String>();
        assertTrue(factory.fromRequest(request, body).isEmpty());

        body.put(RestHttpRequestCredentialFactory.PARAMETER_USERNAME, List.of("user"));
        assertTrue(factory.fromRequest(request, body).isEmpty());

        body.put(DuoSecurityRestHttpRequestCredentialFactory.PARAMETER_NAME_PASSCODE, List.of("123456"));
        body.put(DuoSecurityRestHttpRequestCredentialFactory.PARAMETER_NAME_PROVIDER, List.of("custom-duo"));
        var credentials = factory.fromRequest(request, body);
        assertFalse(credentials.isEmpty());
        val requestCredential = (DuoSecurityPasscodeCredential) credentials.getFirst();
        assertEquals("custom-duo", requestCredential.getProviderId());

        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        credentials = factory.fromAuthentication(request, body, authentication, new TestMultifactorAuthenticationProvider());
        val passcodeCredential = (DuoSecurityPasscodeCredential) credentials.getFirst();
        assertEquals(authentication.getPrincipal().getId(), passcodeCredential.getId());
        assertEquals(TestMultifactorAuthenticationProvider.ID, passcodeCredential.getProviderId());
        assertEquals("123456", passcodeCredential.getPassword());

        body.remove(DuoSecurityRestHttpRequestCredentialFactory.PARAMETER_NAME_PASSCODE);
        credentials = factory.fromAuthentication(request, body, authentication, new TestMultifactorAuthenticationProvider());
        val directCredential = (DuoSecurityDirectCredential) credentials.getFirst();
        assertEquals(TestMultifactorAuthenticationProvider.ID, directCredential.getProviderId());
        assertNotNull(directCredential.getPrincipal());
    }
}

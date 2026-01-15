package org.apereo.cas.trusted.authentication.storage.fingerprint;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.trusted.web.flow.fingerprint.ClientIpDeviceFingerprintExtractor;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ClientIpDeviceFingerprintExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@NoArgsConstructor
@Tag("MFATrustedDevices")
class ClientIpDeviceFingerprintExtractorTests {

    @Test
    void verifyClientIpFingerprintFound() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.setRemoteAddr("1.2.3.4");
        val clientInfo = ClientInfo.from(request);
        ClientInfoHolder.setClientInfo(clientInfo);
        val ex = new ClientIpDeviceFingerprintExtractor();
        assertTrue(ex.extract(RegisteredServiceTestUtils.getAuthentication(), request, response).isPresent());
    }
}

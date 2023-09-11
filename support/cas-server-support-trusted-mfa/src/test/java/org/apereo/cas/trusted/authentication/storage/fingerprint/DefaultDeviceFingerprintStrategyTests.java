package org.apereo.cas.trusted.authentication.storage.fingerprint;

import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDeviceFingerprintStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@SpringBootTest(classes = AbstractMultifactorAuthenticationTrustStorageTests.SharedTestConfiguration.class)
@Tag("MFATrustedDevices")
class DefaultDeviceFingerprintStrategyTests extends AbstractMultifactorAuthenticationTrustStorageTests {

    @Test
    void verifyAction() throws Throwable {
        val context = new MockRequestContext();

        val request = new MockHttpServletRequest();
        request.setRemoteAddr("123.456.789.000");
        request.setLocalAddr("123.456.789.000");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val f1 = deviceFingerprintStrategy.determineFingerprintComponent("casuser", request, response);
        request.setCookies(response.getCookies());
        val f2 = deviceFingerprintStrategy.determineFingerprintComponent("casuser", request, response);
        request.setCookies(response.getCookies());
        assertEquals(f1, f2);

        val f3 = deviceFingerprintStrategy.determineFingerprintComponent("casuser", request, response);
        assertNotNull(response.getCookies());
        assertEquals(1, response.getCookies().length);
        request.setCookies(response.getCookies());

        val f4 = deviceFingerprintStrategy.determineFingerprintComponent("casuser", request, response);
        assertEquals(f3, f4);
    }
}

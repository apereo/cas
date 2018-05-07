package org.apereo.cas.trusted.authentication.storage.fingerprint;

import org.apereo.cas.trusted.AbstractMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultDeviceFingerprintStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultDeviceFingerprintStrategyTests extends AbstractMultifactorAuthenticationTrustStorageTests {
    @Test
    public void verifyAction() {
        final var context = new MockRequestContext();

        final var request = new MockHttpServletRequest();
        request.setRemoteAddr("123.456.789.000");
        request.setLocalAddr("123.456.789.000");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        final var response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        var f1 = deviceFingerprintStrategy.determineFingerprint("casuser", context, false);
        var f2 = deviceFingerprintStrategy.determineFingerprint("casuser", context, false);
        assertNotEquals(f1, f2);

        f1 = deviceFingerprintStrategy.determineFingerprint("casuser", context, true);
        assertNotNull(response.getCookies());
        assertTrue(response.getCookies().length == 1);
        request.setCookies(response.getCookies());
        f2 = deviceFingerprintStrategy.determineFingerprint("casuser", context, false);
        assertEquals(f1, f2);
    }
}

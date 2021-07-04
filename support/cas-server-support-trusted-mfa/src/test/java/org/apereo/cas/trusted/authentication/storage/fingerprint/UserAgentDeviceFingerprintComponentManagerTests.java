package org.apereo.cas.trusted.authentication.storage.fingerprint;

import org.apereo.cas.trusted.web.flow.fingerprint.UserAgentDeviceFingerprintComponentManager;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UserAgentDeviceFingerprintComponentManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MFATrustedDevices")
public class UserAgentDeviceFingerprintComponentManagerTests {
    @Test
    public void verifyAgentFingerprintNotFound() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        ClientInfoHolder.setClientInfo(null);
        val ex = new UserAgentDeviceFingerprintComponentManager();
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertFalse(ex.extractComponent("casuser", request, response).isPresent());
    }

    @Test
    public void verifyAgentFingerprintFound() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.setRemoteAddr("1.2.3.4");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "TestAgent");
        val ex = new UserAgentDeviceFingerprintComponentManager();

        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertTrue(ex.extractComponent("casuser", request, response).isPresent());
    }
}

package org.apereo.cas.trusted.authentication.storage.fingerprint;

import org.apereo.cas.trusted.web.flow.fingerprint.UserAgentDeviceFingerprintComponentManager;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UserAgentDeviceFingerprintComponentManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MFATrustedDevices")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
class UserAgentDeviceFingerprintComponentManagerTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyAgentFingerprintNotFound() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        ClientInfoHolder.setClientInfo(null);
        val ex = new UserAgentDeviceFingerprintComponentManager();
        assertFalse(ex.extractComponent("casuser",
            context.getHttpServletRequest(), context.getHttpServletResponse()).isPresent());
    }

    @Test
    void verifyAgentFingerprintFound() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setRemoteAddr("1.2.3.4");
        context.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "TestAgent");
        val ex = new UserAgentDeviceFingerprintComponentManager();
        assertTrue(ex.extractComponent("casuser",
            context.getHttpServletRequest(), context.getHttpServletResponse()).isPresent());
    }
}

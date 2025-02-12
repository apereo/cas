package org.apereo.cas.trusted.authentication.storage.fingerprint;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.trusted.web.flow.fingerprint.UserAgentDeviceFingerprintExtractor;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UserAgentDeviceFingerprintExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MFATrustedDevices")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class UserAgentDeviceFingerprintExtractorTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyAgentFingerprintNotFound() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val ex = new UserAgentDeviceFingerprintExtractor();
        assertFalse(ex.extract(RegisteredServiceTestUtils.getAuthentication(),
            context.getHttpServletRequest(), context.getHttpServletResponse()).isPresent());
    }

    @Test
    void verifyAgentFingerprintFound() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setRemoteAddr("1.2.3.4");
        context.withUserAgent();
        val ex = new UserAgentDeviceFingerprintExtractor();
        assertTrue(ex.extract(RegisteredServiceTestUtils.getAuthentication(),
            context.getHttpServletRequest(), context.getHttpServletResponse()).isPresent());
    }
}

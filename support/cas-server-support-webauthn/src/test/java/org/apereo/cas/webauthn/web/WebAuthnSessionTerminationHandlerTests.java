package org.apereo.cas.webauthn.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.SessionTerminationHandler;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.webauthn.web.flow.BaseWebAuthnWebflowTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnSessionTerminationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Logout")
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class WebAuthnSessionTerminationHandlerTests {
    @Autowired
    @Qualifier("webAuthnSessionTerminationHandler")
    private SessionTerminationHandler webAuthnSessionTerminationHandler;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        webAuthnSessionTerminationHandler.beforeSessionTermination(requestContext);
        val cookies = requestContext.getHttpServletResponse().getCookies();
        assertEquals(1, cookies.length);
        val cookie = requestContext.getHttpServletResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie);
        assertEquals(0, cookie.getMaxAge());
    }

}

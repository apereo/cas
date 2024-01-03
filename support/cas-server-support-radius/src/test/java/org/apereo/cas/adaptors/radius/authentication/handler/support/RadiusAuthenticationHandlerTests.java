package org.apereo.cas.adaptors.radius.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.RadiusConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RadiusAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RadiusConfiguration.class,
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasWebflowAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAutoConfiguration.class
}, properties = {
    "cas.authn.radius.server.protocol=PAP",
    "cas.authn.radius.client.shared-secret=testing123",
    "cas.authn.radius.client.inet-address=localhost"
})
@Tag("Radius")
@EnabledOnOs(OS.LINUX)
class RadiusAuthenticationHandlerTests {
    @Autowired
    @Qualifier("radiusAuthenticationHandler")
    private AuthenticationHandler radiusAuthenticationHandler;

    @Test
    void verifyOperation() throws Throwable {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = radiusAuthenticationHandler.authenticate(c, mock(Service.class));
        assertNotNull(result);
    }
}

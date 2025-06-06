package org.apereo.cas.logout;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LogoutWebApplicationServiceFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Logout")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = CasCoreLogoutAutoConfigurationTests.SharedTestConfiguration.class,
    properties = "cas.logout.redirect-parameter=url")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class LogoutWebApplicationServiceFactoryTests {
    @Autowired
    @Qualifier("logoutWebApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> logoutWebApplicationServiceFactory;

    @Test
    void verifyOperation() {
        val request = new MockHttpServletRequest();
        assertNull(logoutWebApplicationServiceFactory.createService(request));
        request.setRequestURI(CasProtocolConstants.ENDPOINT_LOGOUT);
        request.addParameter("url", "https://google.com");
        assertNotNull(logoutWebApplicationServiceFactory.createService(request));
    }
}

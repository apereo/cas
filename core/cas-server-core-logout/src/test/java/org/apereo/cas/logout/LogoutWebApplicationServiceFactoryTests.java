package org.apereo.cas.logout;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LogoutWebApplicationServiceFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Logout")
class LogoutWebApplicationServiceFactoryTests {
    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val properties = new LogoutProperties();
        val factory = new LogoutWebApplicationServiceFactory(properties);
        assertNull(factory.getRequestedService(request));

        properties.setRedirectParameter(List.of("url"));
        request.setRequestURI(CasProtocolConstants.ENDPOINT_LOGOUT);
        request.addParameter("url", "https://google.com");
        assertNotNull(factory.getRequestedService(request));
    }
}

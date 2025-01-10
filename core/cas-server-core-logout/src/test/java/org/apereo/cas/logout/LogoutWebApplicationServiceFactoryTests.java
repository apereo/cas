package org.apereo.cas.logout;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link LogoutWebApplicationServiceFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Logout")
class LogoutWebApplicationServiceFactoryTests {
    @Test
    void verifyOperation() {
        val request = new MockHttpServletRequest();
        val properties = new LogoutProperties();
        val factory = new LogoutWebApplicationServiceFactory(mock(TenantExtractor.class), properties);
        assertNull(factory.getRequestedService(request));

        properties.setRedirectParameter(List.of("url"));
        request.setRequestURI(CasProtocolConstants.ENDPOINT_LOGOUT);
        request.addParameter("url", "https://google.com");
        assertNotNull(factory.getRequestedService(request));
    }
}

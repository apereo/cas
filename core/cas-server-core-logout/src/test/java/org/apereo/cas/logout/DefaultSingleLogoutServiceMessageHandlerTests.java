package org.apereo.cas.logout;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.apereo.cas.web.UrlValidator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultSingleLogoutServiceMessageHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Logout")
public class DefaultSingleLogoutServiceMessageHandlerTests {

    @Test
    public void verifyEmpty() {
        val servicesManager = mock(ServicesManager.class);
        val service = new RegexRegisteredService();
        service.setServiceId(UUID.randomUUID().toString());
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(service);

        val handler = new DefaultSingleLogoutServiceMessageHandler(new SimpleHttpClientFactoryBean().getObject(),
            new DefaultSingleLogoutMessageCreator(), servicesManager,
            new DefaultSingleLogoutServiceLogoutUrlBuilder(servicesManager, mock(UrlValidator.class)), false,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
        assertTrue(handler.handle(CoreAuthenticationTestUtils.getWebApplicationService(),
            UUID.randomUUID().toString(), SingleLogoutExecutionRequest.builder().build()).isEmpty());
    }

}

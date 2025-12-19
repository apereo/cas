package org.apereo.cas.logout;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.logout.slo.ChainingSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.SimpleUrlValidator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingSingleLogoutServiceLogoutUrlBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Logout")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = CasCoreLogoutAutoConfigurationTests.SharedTestConfiguration.class)
class ChainingSingleLogoutServiceLogoutUrlBuilderTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Test
    void verifyOperation() {
        val builder = new ChainingSingleLogoutServiceLogoutUrlBuilder(
            List.of(new DefaultSingleLogoutServiceLogoutUrlBuilder(servicesManager, SimpleUrlValidator.getInstance())));
        val service = CoreAuthenticationTestUtils.getWebApplicationService();
        val registeredService = mock(CasRegisteredService.class);
        when(registeredService.matches(any(Service.class))).thenReturn(Boolean.TRUE);
        when(registeredService.getFriendlyName()).thenCallRealMethod();
        when(registeredService.getServiceId()).thenReturn(CoreAuthenticationTestUtils.CONST_TEST_URL);
        when(registeredService.matches(anyString())).thenReturn(Boolean.TRUE);
        when(registeredService.getAccessStrategy()).thenReturn(new DefaultRegisteredServiceAccessStrategy());
        when(registeredService.getLogoutUrl()).thenReturn("https://somewhere.org");
        when(registeredService.getName()).thenReturn(UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        assertTrue(builder.supports(registeredService, service, Optional.empty()));
        assertTrue(builder.isServiceAuthorized(service, Optional.empty(), Optional.empty()));
        assertFalse(builder.determineLogoutUrl(registeredService, service, Optional.empty()).isEmpty());
    }

}

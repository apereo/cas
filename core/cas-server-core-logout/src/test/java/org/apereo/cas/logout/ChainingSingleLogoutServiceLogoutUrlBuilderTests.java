package org.apereo.cas.logout;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.logout.slo.ChainingSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.web.SimpleUrlValidator;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingSingleLogoutServiceLogoutUrlBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Logout")
public class ChainingSingleLogoutServiceLogoutUrlBuilderTests {
    private ServicesManager servicesManager;

    @BeforeEach
    public void beforeEach() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(new InMemoryServiceRegistry(appCtx))
            .applicationContext(appCtx)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
            .build();
        this.servicesManager = new DefaultServicesManager(context);
    }

    @Test
    public void verifyOperation() {
        val builder = new ChainingSingleLogoutServiceLogoutUrlBuilder(
            List.of(new DefaultSingleLogoutServiceLogoutUrlBuilder(servicesManager, SimpleUrlValidator.getInstance())));

        val service = CoreAuthenticationTestUtils.getWebApplicationService();
        val registeredService = mock(RegexRegisteredService.class);
        when(registeredService.matches(any(Service.class))).thenReturn(Boolean.TRUE);
        when(registeredService.getFriendlyName()).thenCallRealMethod();
        when(registeredService.getServiceId()).thenReturn(CoreAuthenticationTestUtils.CONST_TEST_URL);
        when(registeredService.matches(anyString())).thenReturn(Boolean.TRUE);
        when(registeredService.getAccessStrategy()).thenReturn(new DefaultRegisteredServiceAccessStrategy());
        when(registeredService.getLogoutUrl()).thenReturn("https://somewhere.org");
        servicesManager.save(registeredService);

        assertTrue(builder.supports(registeredService, service, Optional.empty()));
        assertTrue(builder.isServiceAuthorized(service, Optional.empty()));
        assertFalse(builder.determineLogoutUrl(registeredService, service, Optional.empty()).isEmpty());
    }

}

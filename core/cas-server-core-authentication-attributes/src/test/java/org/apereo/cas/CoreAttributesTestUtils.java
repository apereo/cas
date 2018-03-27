package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;

import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * This is {@link CoreAttributesTestUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CoreAttributesTestUtils {

    public static final String CONST_USERNAME = "test";
    private static final String CONST_TEST_URL = "https://google.com";

    public static Principal getPrincipal(final String name, final Map<String, Object> attributes) {
        return new DefaultPrincipalFactory().createPrincipal(name, attributes);
    }
    
    public static Service getService() {
        final Service svc = mock(Service.class);
        when(svc.getId()).thenReturn(CONST_TEST_URL);
        when(svc.matches(any(Service.class))).thenReturn(true);
        return svc;
    }

    public static RegisteredService getRegisteredService() {
        final RegisteredService service = mock(RegisteredService.class);
        when(service.getServiceId()).thenReturn(CONST_TEST_URL);
        when(service.getName()).thenReturn("service");
        when(service.getId()).thenReturn(Long.MAX_VALUE);
        when(service.getDescription()).thenReturn("description");

        final RegisteredServiceAccessStrategy access = mock(RegisteredServiceAccessStrategy.class);
        when(access.isServiceAccessAllowed()).thenReturn(true);
        when(service.getAccessStrategy()).thenReturn(access);
        return service;
    }
}

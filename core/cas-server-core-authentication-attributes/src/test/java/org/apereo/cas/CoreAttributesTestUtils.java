package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.util.RandomUtils;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * This is {@link CoreAttributesTestUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@UtilityClass
public class CoreAttributesTestUtils {

    public static final String CONST_USERNAME = "test";

    private static final String CONST_TEST_URL = "https://google.com";

    public static Principal getPrincipal(final String name, final Map<String, List<Object>> attributes) throws Throwable {
        return PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(name, attributes);
    }

    public static Service getService() {
        val svc = mock(Service.class);
        when(svc.getId()).thenReturn(CONST_TEST_URL);
        return svc;
    }

    public static RegisteredService getRegisteredService() {
        return getRegisteredService(CONST_TEST_URL);
    }

    public static RegisteredService getRegisteredService(final String id) {
        val service = mock(RegisteredService.class);
        when(service.getServiceId()).thenReturn(id);
        when(service.getName()).thenReturn("service");
        when(service.getId()).thenReturn(RandomUtils.nextLong());
        when(service.getDescription()).thenReturn("description");

        val access = mock(RegisteredServiceAccessStrategy.class);
        when(access.isServiceAccessAllowed(service, CoreAuthenticationTestUtils.getService())).thenReturn(true);
        when(service.getAccessStrategy()).thenReturn(access);
        return service;
    }
}

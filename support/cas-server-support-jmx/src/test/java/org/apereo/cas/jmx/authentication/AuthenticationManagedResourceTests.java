package org.apereo.cas.jmx.authentication;

import module java.base;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.support.GenericApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationManagedResourceTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("JMX")
@ExtendWith(CasTestExtension.class)
class AuthenticationManagedResourceTests {
    @Test
    void verifyHandlerInventory() {
        try (val context = new GenericApplicationContext()) {
            val handler = mock(AuthenticationHandler.class);
            when(handler.getName()).thenReturn("PasswordHandler");
            when(handler.getOrder()).thenReturn(10);
            val plan = mock(AuthenticationEventExecutionPlan.class);
            when(plan.getAuthenticationHandlers()).thenReturn(Set.of(handler));
            context.getBeanFactory().registerSingleton("plan", plan);
            context.refresh();
            val resource = createResource(context);

            verifyNoInteractions(plan);
            assertArrayEquals(new String[]{"PasswordHandler:10"}, resource.getAuthenticationHandlers());
            assertArrayEquals(ArrayUtils.EMPTY_STRING_ARRAY, resource.getMultifactorAuthenticationProviders());
        }
    }

    @Test
    void verifyMultifactorInventoryAndAvailability() {
        try (val context = new GenericApplicationContext()) {
            val provider = mock(MultifactorAuthenticationProvider.class);
            when(provider.getId()).thenReturn("mfa-example");
            when(provider.getFriendlyName()).thenReturn("Example MFA");
            when(provider.getOrder()).thenReturn(10);
            when(provider.getFailureMode()).thenReturn(
                BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.CLOSED);
            val service = new CasRegisteredService();
            service.setId(42);
            val manager = mock(ServicesManager.class);
            when(manager.findServiceBy(42)).thenReturn(service);
            context.getBeanFactory().registerSingleton("servicesManager", manager);
            context.getBeanFactory().registerSingleton("providerBean", provider);
            context.refresh();
            val resource = createResource(context);

            assertArrayEquals(new String[]{"mfa-example:Example MFA:10:CLOSED"}, resource.getMultifactorAuthenticationProviders());
            verify(provider, never()).isAvailable(any());
            when(provider.isAvailable(service)).thenReturn(true, false);
            assertTrue(resource.isMultifactorAuthenticationProviderAvailable("mfa-example", 42));
            assertFalse(resource.isMultifactorAuthenticationProviderAvailable("mfa-example", 42));
            assertThrows(IllegalArgumentException.class, () -> resource.isMultifactorAuthenticationProviderAvailable("mfa-.*", 42));
            assertThrows(IllegalArgumentException.class, () -> resource.isMultifactorAuthenticationProviderAvailable("providerBean", 42));
            assertThrows(IllegalArgumentException.class, () -> resource.isMultifactorAuthenticationProviderAvailable("mfa-example", 99));
            assertThrows(IllegalArgumentException.class, () -> resource.isMultifactorAuthenticationProviderAvailable(" ", 42));
            verify(provider, times(2)).isAvailable(service);
        }
    }

    @Test
    void verifyProviderFailureIsReported() {
        try (val context = new GenericApplicationContext()) {
            val provider = mock(MultifactorAuthenticationProvider.class);
            when(provider.getId()).thenReturn("mfa-example");
            val service = new CasRegisteredService();
            val manager = mock(ServicesManager.class);
            when(manager.findServiceBy(42)).thenReturn(service);
            when(provider.isAvailable(service)).thenThrow(new IllegalStateException("Unavailable"));
            context.getBeanFactory().registerSingleton("servicesManager", manager);
            context.getBeanFactory().registerSingleton("provider", provider);
            context.refresh();

            assertThrows(IllegalStateException.class,
                () -> createResource(context).isMultifactorAuthenticationProviderAvailable("mfa-example", 42));
        }
    }

    private static AuthenticationManagedResource createResource(final GenericApplicationContext context) {
        return new AuthenticationManagedResource(context.getBeanProvider(AuthenticationEventExecutionPlan.class),
            context.getBeanProvider(ServicesManager.class), context);
    }
}

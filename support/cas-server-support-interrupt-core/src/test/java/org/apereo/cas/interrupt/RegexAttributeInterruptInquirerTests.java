package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegexAttributeInterruptInquirerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RegexAttributeInterruptInquirerTests {
    @Test
    public void verifyResponseCanBeFoundFromAttributes() {
        final var q =
            new RegexAttributeInterruptInquirer("member..", "CA.|system");
        final var response = q.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            CoreAuthenticationTestUtils.getRegisteredService(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertNotNull(response);
        assertFalse(response.isBlock());
        assertTrue(response.isSsoEnabled());
        assertTrue(response.isInterrupt());
    }

    @Test
    public void verifyInterruptSkipped() {
        final var q =
            new RegexAttributeInterruptInquirer("member..", "CA.|system");
        final var registeredService = CoreAuthenticationTestUtils.getRegisteredService();

        final Map<String, RegisteredServiceProperty> properties = new LinkedHashMap<>();
        final var value = new DefaultRegisteredServiceProperty();
        value.addValue(Boolean.TRUE.toString());
        properties.put(RegisteredServiceProperty.RegisteredServiceProperties.SKIP_INTERRUPT_NOTIFICATIONS.getPropertyName(), value);
        when(registeredService.getProperties()).thenReturn(properties);
        final var response = q.inquire(CoreAuthenticationTestUtils.getAuthentication("casuser"),
            registeredService,
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertNotNull(response);
        assertFalse(response.isInterrupt());
    }
}

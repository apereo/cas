package org.apereo.cas.services;

import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServicePropertyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
public class RegisteredServicePropertyTests {

    @Test
    public void verifyNull() {
        val p1 = new DefaultRegisteredServiceProperty(null);
        assertNull(p1.getValue(String.class));
        assertFalse(p1.getBooleanValue());
    }

    @Test
    public void verifyValue() {
        val p1 = new DefaultRegisteredServiceProperty("true");
        assertEquals("true", p1.getValue(String.class));
        assertTrue(p1.getBooleanValue());
    }

    @Test
    public void verifyTypedValue() {
        val service = mock(RegisteredService.class);
        val properties = (Map) Map.of(
            RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_CIPHER_STRATEGY_TYPE.getPropertyName(), new DefaultRegisteredServiceProperty("ENCRYPT_AND_SIGN"),
            RegisteredServiceProperties.CORS_MAX_AGE.getPropertyName(), new DefaultRegisteredServiceProperty("100"),
            RegisteredServiceProperties.DELEGATED_AUTHN_SAML2_ASSERTION_CONSUMER_SERVICE_INDEX.getPropertyName(), new DefaultRegisteredServiceProperty("1"),
            RegisteredServiceProperties.DELEGATED_AUTHN_SAML2_AUTHN_CONTEXT_CLASS_REFS.getPropertyName(), new DefaultRegisteredServiceProperty("class1"),
            RegisteredServiceProperties.DELEGATED_AUTHN_SAML2_MAXIMUM_AUTHN_LIFETIME.getPropertyName(), new DefaultRegisteredServiceProperty("100"),
            RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED.getPropertyName(), new DefaultRegisteredServiceProperty("true"));

        when(service.getProperties()).thenReturn(properties);

        assertNotNull(RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_CIPHER_STRATEGY_TYPE.getTypedPropertyValue(service));
        assertNotNull(RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_ENABLED.getTypedPropertyValue(service));
        assertNotNull(RegisteredServiceProperties.CORS_MAX_AGE.getTypedPropertyValue(service));
        assertNotNull(RegisteredServiceProperties.DELEGATED_AUTHN_SAML2_ASSERTION_CONSUMER_SERVICE_INDEX.getTypedPropertyValue(service));
        assertNotNull(RegisteredServiceProperties.DELEGATED_AUTHN_SAML2_AUTHN_CONTEXT_CLASS_REFS.getTypedPropertyValue(service));
        assertNotNull(RegisteredServiceProperties.DELEGATED_AUTHN_SAML2_MAXIMUM_AUTHN_LIFETIME.getTypedPropertyValue(service));
    }

    @Getter(onMethod = @__({@Override}))
    @RequiredArgsConstructor
    private static class DefaultRegisteredServiceProperty implements RegisteredServiceProperty {
        private static final long serialVersionUID = -4878764188998002053L;

        private final String value;

        @Override
        public Set<String> getValues() {
            return Set.of();
        }

        @Override
        public boolean contains(final String value) {
            return false;
        }
    }
}

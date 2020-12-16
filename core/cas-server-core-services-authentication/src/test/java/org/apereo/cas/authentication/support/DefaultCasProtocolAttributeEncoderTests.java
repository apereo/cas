package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.validation.ValidationResponseType;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultCasProtocolAttributeEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Attributes")
public class DefaultCasProtocolAttributeEncoderTests {
    private final ProtocolAttributeEncoder encoder = new DefaultCasProtocolAttributeEncoder(mock(ServicesManager.class),
        CipherExecutor.noOpOfStringToString());

    private RegisteredService registeredService;
    private WebApplicationService webApplicationService;

    @BeforeEach
    public void initialize() {
        this.registeredService = mock(RegisteredService.class);
        when(registeredService.getId()).thenReturn(1L);
        when(registeredService.getServiceId()).thenReturn("https://www.google.com/.+");
        when(registeredService.getAccessStrategy()).thenReturn(new DefaultRegisteredServiceAccessStrategy());

        this.webApplicationService = mock(WebApplicationService.class);
        when(webApplicationService.getId()).thenReturn("https://www.google.com/");
        when(webApplicationService.getOriginalUrl()).thenReturn("https://www.google.com/");
    }

    @Test
    public void verifyEncodeNamesCorrectly() {
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("user@name", "casuser");
        attributes.put("user:name", "casuser");
        val results = encoder.encodeAttributes(attributes, registeredService, webApplicationService);
        assertFalse(results.containsKey("user@name"));
        assertFalse(results.containsKey("user:name"));
    }

    @Test
    public void verifyEncodeNamesUnnecessary() {
        when(webApplicationService.getFormat()).thenReturn(ValidationResponseType.JSON);
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("user@name", "casuser");
        attributes.put("user:name", "casuser");
        val results = encoder.encodeAttributes(attributes, registeredService, webApplicationService);
        assertTrue(results.containsKey("user@name"));
        assertTrue(results.containsKey("user:name"));
    }

    @Test
    public void verifyEncodeNamesWithNoService() {
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("user@name", "casuser");
        attributes.put("user:name", "casuser");
        val results = encoder.encodeAttributes(attributes, null, webApplicationService);
        assertEquals(2, results.size());
    }

    @Test
    public void verifyEncodeBinaryValuesCorrectly() {
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("user", "casuser".getBytes(StandardCharsets.UTF_8));
        val results = encoder.encodeAttributes(attributes, registeredService, webApplicationService);
        assertTrue(results.containsKey("user"));
        val user = results.get("user");
        assertTrue(user.getClass().isAssignableFrom(String.class));
    }
}

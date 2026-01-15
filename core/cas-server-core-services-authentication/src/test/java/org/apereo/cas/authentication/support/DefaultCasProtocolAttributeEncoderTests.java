package org.apereo.cas.authentication.support;

import module java.base;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServicePublicKeyCipherExecutor;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.validation.ValidationResponseType;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultCasProtocolAttributeEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Attributes")
class DefaultCasProtocolAttributeEncoderTests {
    private final ProtocolAttributeEncoder encoder = new DefaultCasProtocolAttributeEncoder(mock(ServicesManager.class),
        RegisteredServicePublicKeyCipherExecutor.INSTANCE, CipherExecutor.noOpOfStringToString());

    private RegisteredService registeredService;

    private WebApplicationService webApplicationService;

    @BeforeEach
    void initialize() {
        this.registeredService = mock(RegisteredService.class);
        when(registeredService.getId()).thenReturn(1L);
        when(registeredService.getServiceId()).thenReturn("https://www.google.com/.+");
        when(registeredService.getAccessStrategy()).thenReturn(new DefaultRegisteredServiceAccessStrategy());
        when(registeredService.getPublicKey()).thenReturn(new RegisteredServicePublicKeyImpl("classpath:keys/RSA1024Public.key", "RSA"));

        this.webApplicationService = mock(WebApplicationService.class);
        when(webApplicationService.getId()).thenReturn("https://www.google.com/");
        when(webApplicationService.getOriginalUrl()).thenReturn("https://www.google.com/");
        when(webApplicationService.getFormat()).thenReturn(ValidationResponseType.XML);
    }

    @Test
    void verifyEncodeNamesCorrectly() {
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("user@name", "casuser");
        attributes.put("user:name", "casuser");
        val results = encoder.encodeAttributes(Map.of(), attributes, registeredService, webApplicationService);
        assertFalse(results.containsKey("user@name"));
        assertFalse(results.containsKey("user:name"));
    }

    @Test
    void verifyEncodeNamesUnnecessary() {
        when(webApplicationService.getFormat()).thenReturn(ValidationResponseType.JSON);
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("user@name", "casuser");
        attributes.put("user:name", "casuser");
        val results = encoder.encodeAttributes(Map.of(), attributes, registeredService, webApplicationService);
        assertTrue(results.containsKey("user@name"));
        assertTrue(results.containsKey("user:name"));
    }

    @Test
    void verifyEncodeNamesWithNoService() {
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("user@name", "casuser");
        attributes.put("user:name", "casuser");
        val results = encoder.encodeAttributes(Map.of(), attributes, null, webApplicationService);
        assertEquals(2, results.size());
    }

    @Test
    void verifyEncodeBinaryValuesCorrectly() {
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("user", "casuser".getBytes(StandardCharsets.UTF_8));
        val results = encoder.encodeAttributes(Map.of(), attributes, registeredService, webApplicationService);
        assertTrue(results.containsKey("user"));
        val user = results.get("user");
        assertTrue(user.getClass().isAssignableFrom(String.class));
    }

    @Test
    void verifyEncodedWithPrefix() {
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("_MAIL_", "casuser@example.org");
        val results = encoder.encodeAttributes(Map.of(), attributes, registeredService, webApplicationService);
        ProtocolAttributeEncoder.decodeAttributes(results, registeredService, webApplicationService);
    }

    @Test
    void verifyProxyGrantingTicket() {
        val pgt = mock(Ticket.class);
        val pgtId = UUID.randomUUID().toString();
        when(pgt.getId()).thenReturn(pgtId);
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("user@name", "casuser");
        attributes.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, List.of(pgtId));
        var results = encoder.encodeAttributes(Map.of(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, pgt),
            attributes, registeredService, webApplicationService);
        assertEquals(2, results.size());
        assertTrue(results.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET));
        assertNotEquals(pgtId, results.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET));

        when(pgt.isStateless()).thenReturn(true);
        results = encoder.encodeAttributes(Map.of(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET, pgt),
            attributes, registeredService, webApplicationService);
        assertEquals(2, results.size());
        assertTrue(results.containsKey(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET));
        assertEquals(pgtId, results.get(CasViewConstants.MODEL_ATTRIBUTE_NAME_PROXY_GRANTING_TICKET));
    }

    
}

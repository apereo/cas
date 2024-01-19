package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ProtocolAttributeEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Attributes")
class ProtocolAttributeEncoderTests {
    @Test
    void verifyEncoder() throws Throwable {
        val encoder = new ProtocolAttributeEncoder() {
        };
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put(ProtocolAttributeEncoder.encodeAttribute("user@name"), "casuser");
        val results = encoder.encodeAttributes(Map.of(), attributes, mock(RegisteredService.class), mock(WebApplicationService.class));
        assertTrue(results.containsKey("user@name"));
    }
}

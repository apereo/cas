package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 5.0
 */
public class ReturnMappedAttributeReleasePolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "returnMappedAttributeReleasePolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAReturnMappedAttributeReleasePolicyToJson() throws IOException {
        final HashMap<String, String> allowedAttributes = new HashMap<>();
        allowedAttributes.put("keyOne", "valueOne");
        final ReturnMappedAttributeReleasePolicy policyWritten = new ReturnMappedAttributeReleasePolicy(allowedAttributes);

        MAPPER.writeValue(JSON_FILE, policyWritten);
        final RegisteredServiceAttributeReleasePolicy policyRead = MAPPER.readValue(JSON_FILE, ReturnMappedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifyInlinedGroovyAttributes() throws IOException {
        final Map<String, String> allowedAttributes = new HashMap<>();
        allowedAttributes.put("attr1", "groovy { logger.debug('Running script...'); return 'DOMAIN\\\\' + attributes['uid'] }");
        final ReturnMappedAttributeReleasePolicy policyWritten = new ReturnMappedAttributeReleasePolicy(allowedAttributes);
        final RegisteredService registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policyWritten);
        final Map<String, Object> principalAttributes = new HashMap<>();
        principalAttributes.put("uid", CoreAuthenticationTestUtils.CONST_USERNAME);
        final Map<String, Object> result = policyWritten.getAttributes(
                CoreAuthenticationTestUtils.getPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME, principalAttributes),
                CoreAuthenticationTestUtils.getService(), registeredService);
        assertTrue(result.containsKey("attr1"));
        assertTrue(result.containsValue("DOMAIN\\" + CoreAuthenticationTestUtils.CONST_USERNAME));
    }
}

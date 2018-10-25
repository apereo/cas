package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class PrincipalAttributeRegisteredServiceUsernameProviderTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "principalAttributeRegisteredServiceUsernameProvider.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyUsernameByPrincipalAttributeWithMapping() {
        val provider =
            new PrincipalAttributeRegisteredServiceUsernameProvider("email");

        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        val mappedAttribute = "urn:oid:0.9.2342.19200300.100.1.3";
        allowedAttributes.put("email", mappedAttribute);
        val policy = new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(allowedAttributes));
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        registeredService.setAttributeReleasePolicy(policy);

        val principalAttributes = new HashMap<String, Object>();
        principalAttributes.put("email", "user@example.org");
        val p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(principalAttributes);
        val id = provider.resolveUsername(p,
            RegisteredServiceTestUtils.getService("verifyUsernameByPrincipalAttributeWithMapping"), registeredService);
        assertEquals("user@example.org", id);
    }

    @Test
    public void verifyUsernameByPrincipalAttributeAsCollection() {
        val provider =
            new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        val attrs = new HashMap<String, Object>();
        attrs.put("userid", CollectionUtils.wrap("u1"));
        attrs.put("cn", CollectionUtils.wrap("TheName"));

        val p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);

        val id = provider.resolveUsername(p, RegisteredServiceTestUtils.getService("usernameAttributeProviderService"),
            RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"));
        assertEquals("TheName", id);
    }

    @Test
    public void verifyUsernameByPrincipalAttribute() {
        val provider =
            new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        val attrs = new HashMap<String, Object>();
        attrs.put("userid", "u1");
        attrs.put("cn", "TheName");

        val p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);

        val id = provider.resolveUsername(p, RegisteredServiceTestUtils.getService("usernameAttributeProviderService"),
            RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"));
        assertEquals("TheName", id);
    }

    @Test
    public void verifyUsernameByPrincipalAttributeNotFound() {
        val provider =
            new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        val attrs = new HashMap<String, Object>();
        attrs.put("userid", "u1");

        val p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);

        val id = provider.resolveUsername(p, RegisteredServiceTestUtils.getService("usernameAttributeProviderService"),
            RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"));
        assertEquals(id, p.getId());
    }

    @Test
    public void verifyEquality() {
        val provider =
            new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        val provider2 =
            new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        assertEquals(provider, provider2);
    }

    @Test
    public void verifySerializeAPrincipalAttributeRegisteredServiceUsernameProviderToJson() throws IOException {
        val providerWritten =
            new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        MAPPER.writeValue(JSON_FILE, providerWritten);
        val providerRead = MAPPER.readValue(JSON_FILE, PrincipalAttributeRegisteredServiceUsernameProvider.class);
        assertEquals(providerWritten, providerRead);
    }
}

package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = {
    CasCoreUtilConfiguration.class,
    RefreshAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class PrincipalAttributeRegisteredServiceUsernameProviderTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "principalAttributeRegisteredServiceUsernameProvider.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyUsernameByPrincipalAttributeWithMapping() {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider("email");

        val allowedAttributes = ArrayListMultimap.<String, Object>create();
        val mappedAttribute = "urn:oid:0.9.2342.19200300.100.1.3";
        allowedAttributes.put("email", List.of(mappedAttribute));
        val policy = new ReturnMappedAttributeReleasePolicy(CollectionUtils.wrap(allowedAttributes));
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        registeredService.setAttributeReleasePolicy(policy);

        val principalAttributes = new HashMap<String, List<Object>>();
        principalAttributes.put("email", List.of("user@example.org"));
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

        val attrs = new HashMap<String, List<Object>>();
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
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        val attrs = new HashMap<String, List<Object>>();
        attrs.put("userid", List.of("u1"));
        attrs.put("cn", List.of("TheName"));

        val p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);

        val id = provider.resolveUsername(p, RegisteredServiceTestUtils.getService("usernameAttributeProviderService"),
            RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"));
        assertEquals("TheName", id);
    }

    @Test
    public void verifyNoAttrRelPolicy() {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        val attrs = new HashMap<String, List<Object>>();
        attrs.put("userid", List.of("u1"));
        attrs.put("cn", List.of("TheName"));

        val p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);

        val service = RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService");
        service.setAttributeReleasePolicy(null);
        val id = provider.resolveUsername(p, RegisteredServiceTestUtils.getService("usernameAttributeProviderService"), service);
        assertEquals("TheName", id);
    }

    @Test
    public void verifyDisabledService() {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        val attrs = new HashMap<String, List<Object>>();
        attrs.put("userid", List.of("u1"));
        attrs.put("cn", List.of("TheName"));

        val p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);

        val service = RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService");
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
        service.setAttributeReleasePolicy(null);
        assertThrows(UnauthorizedServiceException.class,
            () -> provider.resolveUsername(p, RegisteredServiceTestUtils.getService("usernameAttributeProviderService"), service));
    }

    @Test
    public void verifyUsernameByPrincipalAttributeNotFound() {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        val attrs = new HashMap<String, List<Object>>();
        attrs.put("userid", List.of("u1"));

        val p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);

        val id = provider.resolveUsername(p, RegisteredServiceTestUtils.getService("usernameAttributeProviderService"),
            RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"));
        assertEquals(id, p.getId());
    }

    @Test
    public void verifyUsernameUndefined() {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider();
        val p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        val id = provider.resolveUsername(p, RegisteredServiceTestUtils.getService("usernameAttributeProviderService"),
            RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"));
        assertEquals(id, p.getId());
    }

    @Test
    public void verifyEquality() {
        val provider = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        val provider2 = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        assertEquals(provider, provider2);
    }

    @Test
    public void verifySerializeAPrincipalAttributeRegisteredServiceUsernameProviderToJson() throws IOException {
        val providerWritten = new PrincipalAttributeRegisteredServiceUsernameProvider("cn");
        MAPPER.writeValue(JSON_FILE, providerWritten);
        val providerRead = MAPPER.readValue(JSON_FILE, PrincipalAttributeRegisteredServiceUsernameProvider.class);
        assertEquals(providerWritten, providerRead);
    }
}

package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class PrincipalAttributeRegisteredServiceUsernameProviderTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "principalAttributeRegisteredServiceUsernameProvider.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifyUsernameByPrincipalAttribute() {
        final PrincipalAttributeRegisteredServiceUsernameProvider provider =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("userid", "u1");
        attrs.put("cn", "TheName");

        final Principal p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);

        final String id = provider.resolveUsername(p, RegisteredServiceTestUtils.getService("usernameAttributeProviderService"),
                RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"));
        assertEquals(id, "TheName");
    }

    @Test
    public void verifyUsernameByPrincipalAttributeNotFound() {
        final PrincipalAttributeRegisteredServiceUsernameProvider provider =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("userid", "u1");

        final Principal p = mock(Principal.class);
        when(p.getId()).thenReturn("person");
        when(p.getAttributes()).thenReturn(attrs);

        final String id = provider.resolveUsername(p, RegisteredServiceTestUtils.getService("usernameAttributeProviderService"),
                RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"));
        assertEquals(id, p.getId());
    }

    @Test
    public void verifyEquality() {
        final PrincipalAttributeRegisteredServiceUsernameProvider provider =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        final PrincipalAttributeRegisteredServiceUsernameProvider provider2 =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        assertEquals(provider, provider2);
    }

    @Test
    public void verifySerializeAPrincipalAttributeRegisteredServiceUsernameProviderToJson() throws IOException {
        final PrincipalAttributeRegisteredServiceUsernameProvider providerWritten =
                new PrincipalAttributeRegisteredServiceUsernameProvider("cn");

        MAPPER.writeValue(JSON_FILE, providerWritten);

        final RegisteredServiceUsernameAttributeProvider providerRead = MAPPER.readValue(JSON_FILE, PrincipalAttributeRegisteredServiceUsernameProvider.class);

        assertEquals(providerWritten, providerRead);
    }
}

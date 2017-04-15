package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class DefaultRegisteredServiceUsernameProviderTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultRegisteredServiceUsernameProvider.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifyRegServiceUsernameUpper() {
        final DefaultRegisteredServiceUsernameProvider provider = new DefaultRegisteredServiceUsernameProvider();
        provider.setCanonicalizationMode(CaseCanonicalizationMode.UPPER.name());
        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn("id");
        final String id = provider.resolveUsername(principal, RegisteredServiceTestUtils.getService(),
                RegisteredServiceTestUtils.getRegisteredService("usernameAttributeProviderService"));
        assertEquals(id, principal.getId().toUpperCase());
    }

    @Test
    public void verifyRegServiceUsername() {
        final DefaultRegisteredServiceUsernameProvider provider = new DefaultRegisteredServiceUsernameProvider();

        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn("id");
        final String id = provider.resolveUsername(principal, RegisteredServiceTestUtils.getService(),
                RegisteredServiceTestUtils.getRegisteredService("id"));
        assertEquals(id, principal.getId());
    }

    @Test
    public void verifyEquality() {
        final DefaultRegisteredServiceUsernameProvider provider =
                new DefaultRegisteredServiceUsernameProvider();

        final DefaultRegisteredServiceUsernameProvider provider2 =
                new DefaultRegisteredServiceUsernameProvider();

        assertEquals(provider, provider2);
    }

    @Test
    public void verifySerializeADefaultRegisteredServiceUsernameProviderToJson() throws IOException {
        final DefaultRegisteredServiceUsernameProvider providerWritten = new DefaultRegisteredServiceUsernameProvider();

        MAPPER.writeValue(JSON_FILE, providerWritten);

        final RegisteredServiceUsernameAttributeProvider providerRead = MAPPER.readValue(JSON_FILE, DefaultRegisteredServiceUsernameProvider.class);

        assertEquals(providerWritten, providerRead);
    }
}

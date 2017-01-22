package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class AnonymousRegisteredServiceUsernameAttributeProviderTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "anonymousRegisteredServiceUsernameAttributeProvider.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifyPrincipalResolution() {
        final AnonymousRegisteredServiceUsernameAttributeProvider provider =
                new AnonymousRegisteredServiceUsernameAttributeProvider(
                new ShibbolethCompatiblePersistentIdGenerator("casrox"));
        
        final Service service = mock(Service.class);
        when(service.getId()).thenReturn("id");
        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn("uid");
        final String id = provider.resolveUsername(principal, service);
        assertNotNull(id);
    }

    @Test
    public void verifyEquality() {
        final AnonymousRegisteredServiceUsernameAttributeProvider provider =
                new AnonymousRegisteredServiceUsernameAttributeProvider(
                        new ShibbolethCompatiblePersistentIdGenerator("casrox"));

        final AnonymousRegisteredServiceUsernameAttributeProvider provider2 =
                new AnonymousRegisteredServiceUsernameAttributeProvider(
                        new ShibbolethCompatiblePersistentIdGenerator("casrox"));

        assertEquals(provider, provider2);
    }

    @Test
    public void verifySerializeADefaultRegisteredServiceUsernameProviderToJson() throws IOException {
        final AnonymousRegisteredServiceUsernameAttributeProvider providerWritten =
                new AnonymousRegisteredServiceUsernameAttributeProvider(
                        new ShibbolethCompatiblePersistentIdGenerator("casrox"));

        MAPPER.writeValue(JSON_FILE, providerWritten);

        final RegisteredServiceUsernameAttributeProvider providerRead = MAPPER.readValue(JSON_FILE, AnonymousRegisteredServiceUsernameAttributeProvider.class);

        assertEquals(providerWritten, providerRead);
    }
}

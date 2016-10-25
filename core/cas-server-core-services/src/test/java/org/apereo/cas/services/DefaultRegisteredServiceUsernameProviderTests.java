package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.authentication.principal.Principal;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class DefaultRegisteredServiceUsernameProviderTests {

    private static final File JSON_FILE = new File("defaultRegisteredServiceUsernameProvider.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifyRegServiceUsername() {
        final DefaultRegisteredServiceUsernameProvider provider =
                new DefaultRegisteredServiceUsernameProvider();

        final Principal principal = mock(Principal.class);
        when(principal.getId()).thenReturn("id");
        final String id = provider.resolveUsername(principal, TestUtils.getService());
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

        mapper.writeValue(JSON_FILE, providerWritten);

        final RegisteredServiceUsernameAttributeProvider providerRead = mapper.readValue(JSON_FILE, DefaultRegisteredServiceUsernameProvider.class);

        assertEquals(providerWritten, providerRead);
    }
}

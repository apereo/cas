package org.apereo.cas.adaptors.trusted.authentication.principal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class PrincipalBearingCredentialsTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "principalBearingCredential.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PrincipalBearingCredential principalBearingCredentials;

    @Before
    public void setUp() {
        this.principalBearingCredentials = new PrincipalBearingCredential(new DefaultPrincipalFactory().createPrincipal("test"));
    }

    @Test
    public void verifyGetOfPrincipal() {
        assertEquals("test", this.principalBearingCredentials.getPrincipal().getId());
    }

    @Test
    public void verifySerializeAPrincipalBearingCredentialToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, principalBearingCredentials);

        final CredentialMetaData credentialRead = MAPPER.readValue(JSON_FILE, PrincipalBearingCredential.class);

        assertEquals(principalBearingCredentials, credentialRead);
    }
}

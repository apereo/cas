package org.apereo.cas.adaptors.trusted.authentication.principal;

import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class PrincipalBearingCredentialsTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "principalBearingCredential.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PrincipalBearingCredential principalBearingCredentials;

    @BeforeEach
    public void initialize() {
        this.principalBearingCredentials = new PrincipalBearingCredential(new DefaultPrincipalFactory().createPrincipal("test"));
    }

    @Test
    public void verifyGetOfPrincipal() {
        assertEquals("test", this.principalBearingCredentials.getPrincipal().getId());
    }

    @Test
    public void verifySerializeAPrincipalBearingCredentialToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, principalBearingCredentials);
        val credentialRead = MAPPER.readValue(JSON_FILE, PrincipalBearingCredential.class);
        assertEquals(principalBearingCredentials, credentialRead);
    }
}

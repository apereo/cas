package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link DefaultPrincipalAttributesRepository}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultPrincipalAttributesRepositoryTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultPrincipalAttributesRepository.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PrincipalFactory factory = new DefaultPrincipalFactory();

    @Test
    public void checkNoAttributes() {
        final PrincipalAttributesRepository rep = new DefaultPrincipalAttributesRepository();
        assertEquals(rep.getAttributes(this.factory.createPrincipal("uid")).size(), 0);
    }

    @Test
    public void checkInitialAttributes() {
        final Principal p = this.factory.createPrincipal("uid",
                Collections.singletonMap("mail", "final@example.com"));
        final PrincipalAttributesRepository rep = new DefaultPrincipalAttributesRepository();
        assertEquals(rep.getAttributes(p).size(), 1);
        assertTrue(rep.getAttributes(p).containsKey("mail"));
    }

    @Test
    public void verifySerializeADefaultPrincipalAttributesRepositoryToJson() throws IOException {
        final PrincipalAttributesRepository repositoryWritten = new DefaultPrincipalAttributesRepository();

        MAPPER.writeValue(JSON_FILE, repositoryWritten);

        final PrincipalAttributesRepository repositoryRead = MAPPER.readValue(JSON_FILE, DefaultPrincipalAttributesRepository.class);

        assertEquals(repositoryWritten, repositoryRead);
    }
}

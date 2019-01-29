package org.apereo.cas;

import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Handles tests for {@link DefaultPrincipalAttributesRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultPrincipalAttributesRepositoryTests extends BaseCasCoreTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultPrincipalAttributesRepository.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("principalFactory")
    private ObjectProvider<PrincipalFactory> principalFactory;

    @Test
    public void checkDefaultAttributes() {
        val rep = new DefaultPrincipalAttributesRepository();
        assertEquals(3, rep.getAttributes(this.principalFactory.getIfAvailable().createPrincipal("uid")).size());
    }

    @Test
    public void checkInitialAttributes() {
        val p = this.principalFactory.getIfAvailable().createPrincipal("uid", Collections.singletonMap("mail", "final@example.com"));
        val rep = new DefaultPrincipalAttributesRepository();
        assertEquals(1, rep.getAttributes(p).size());
        assertTrue(rep.getAttributes(p).containsKey("mail"));
    }

    @Test
    public void verifySerializeADefaultPrincipalAttributesRepositoryToJson() throws IOException {
        val repositoryWritten = new DefaultPrincipalAttributesRepository();
        MAPPER.writeValue(JSON_FILE, repositoryWritten);
        val repositoryRead = MAPPER.readValue(JSON_FILE, DefaultPrincipalAttributesRepository.class);
        assertEquals(repositoryWritten, repositoryRead);
    }
}

package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Handles tests for {@link DefaultPrincipalAttributesRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Simple")
public class DefaultPrincipalAttributesRepositoryTests extends BaseCasCoreTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultPrincipalAttributesRepository.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("principalFactory")
    private ObjectProvider<PrincipalFactory> principalFactory;

    @Test
    public void checkDefaultAttributes() {
        val rep = new DefaultPrincipalAttributesRepository();
        val principal = CoreAuthenticationTestUtils.getPrincipal();
        assertEquals(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap().size(),
            rep.getAttributes(principal, CoreAuthenticationTestUtils.getRegisteredService()).size());
    }

    @Test
    public void checkInitialAttributes() {
        val p = this.principalFactory.getObject().createPrincipal("uid", Collections.singletonMap("mail", List.of("final@example.com")));
        val rep = new DefaultPrincipalAttributesRepository();
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        assertEquals(1, rep.getAttributes(p, registeredService).size());
        assertTrue(rep.getAttributes(p, registeredService).containsKey("mail"));
    }

    @Test
    public void verifySerializeADefaultPrincipalAttributesRepositoryToJson() throws IOException {
        val repositoryWritten = new DefaultPrincipalAttributesRepository();
        repositoryWritten.setIgnoreResolvedAttributes(true);
        repositoryWritten.setAttributeRepositoryIds(CollectionUtils.wrapSet("1", "2", "3"));
        MAPPER.writeValue(JSON_FILE, repositoryWritten);
        val repositoryRead = MAPPER.readValue(JSON_FILE, DefaultPrincipalAttributesRepository.class);
        assertEquals(repositoryWritten, repositoryRead);
    }
}

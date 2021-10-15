package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Handles tests for {@link DefaultPrincipalAttributesRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Attributes")
public class DefaultPrincipalAttributesRepositoryTests extends BaseCasCoreTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultPrincipalAttributesRepository.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void checkDefaultAttributes() {
        val rep = new DefaultPrincipalAttributesRepository();
        val principal = CoreAuthenticationTestUtils.getPrincipal();
        assertEquals(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap().size(),
            rep.getAttributes(principal, CoreAuthenticationTestUtils.getRegisteredService()).size());
    }

    @Test
    public void checkInitialAttributes() {
        val p = PrincipalFactoryUtils.newPrincipalFactory()
            .createPrincipal("uid", Collections.singletonMap("mail", List.of("final@example.com")));
        val rep = new DefaultPrincipalAttributesRepository();
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        assertEquals(1, rep.getAttributes(p, registeredService).size());
        assertTrue(rep.getAttributes(p, registeredService).containsKey("mail"));
    }

    @Test
    public void checkAttributesWithRepository() {
        val p = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("uid",
            Collections.singletonMap("mail", List.of("final@example.com")));
        val rep = new DefaultPrincipalAttributesRepository();
        rep.setMergingStrategy(PrincipalAttributesCoreProperties.MergingStrategyTypes.NONE);
        rep.setAttributeRepositoryIds(Set.of("StubPersonAttributeDao"));

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

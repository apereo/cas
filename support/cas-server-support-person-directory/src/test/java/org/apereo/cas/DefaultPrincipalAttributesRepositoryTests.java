package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
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
@Import(CasPersonDirectoryTestConfiguration.class)
class DefaultPrincipalAttributesRepositoryTests extends BaseCasCoreTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultPrincipalAttributesRepository.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void checkDefaultAttributes() {
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .build();
        try (val rep = new DefaultPrincipalAttributesRepository()) {
            assertEquals(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap().size(), rep.getAttributes(context).size());
        }
    }

    @Test
    void checkInitialAttributes() throws Throwable {
        val principal = PrincipalFactoryUtils.newPrincipalFactory()
            .createPrincipal("uid", Collections.singletonMap("mail", List.of("final@example.com")));
        try (val rep = new DefaultPrincipalAttributesRepository()) {
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .principal(principal)
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
                .build();
            assertEquals(1, rep.getAttributes(context).size());
            assertTrue(rep.getAttributes(context).containsKey("mail"));
        }
    }

    @Test
    void checkAttributesWithRepository() throws Throwable {
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("uid",
            Collections.singletonMap("mail", List.of("final@example.com")));
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .applicationContext(applicationContext)
            .principal(principal)
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .build();

        try (val rep = new DefaultPrincipalAttributesRepository()) {
            rep.setMergingStrategy(PrincipalAttributesCoreProperties.MergingStrategyTypes.SOURCE);
            rep.setAttributeRepositoryIds(Set.of("StubPersonAttributeDao"));
            assertEquals(1, rep.getAttributes(context).size());
            assertTrue(rep.getAttributes(context).containsKey("mail"));
        }
    }

    @Test
    void verifySerializeADefaultPrincipalAttributesRepositoryToJson() throws IOException {
        val repositoryWritten = new DefaultPrincipalAttributesRepository();
        repositoryWritten.setIgnoreResolvedAttributes(true);
        repositoryWritten.setAttributeRepositoryIds(CollectionUtils.wrapSet("1", "2", "3"));
        MAPPER.writeValue(JSON_FILE, repositoryWritten);
        val repositoryRead = MAPPER.readValue(JSON_FILE, DefaultPrincipalAttributesRepository.class);
        assertEquals(repositoryWritten, repositoryRead);
    }
}

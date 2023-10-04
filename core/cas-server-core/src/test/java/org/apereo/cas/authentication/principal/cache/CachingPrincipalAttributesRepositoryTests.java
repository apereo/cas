package org.apereo.cas.authentication.principal.cache;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Handles tests for {@link CachingPrincipalAttributesRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Attributes")
class CachingPrincipalAttributesRepositoryTests {
    private static final String MAIL = "mail";

    private static final Map<String, List<Object>> REPOSITORY_ATTRIBUTES;

    private static final Principal PRINCIPAL;

    static {
        REPOSITORY_ATTRIBUTES = new HashMap<>();
        REPOSITORY_ATTRIBUTES.put("a1", Arrays.asList("v1", "v2", "v3"));

        var email = new ArrayList<>();
        email.add("final@example.com");
        REPOSITORY_ATTRIBUTES.put(MAIL, email);

        REPOSITORY_ATTRIBUTES.put("a6", Arrays.asList("v16", "v26", "v63"));
        REPOSITORY_ATTRIBUTES.put("a2", List.of("v4"));
        REPOSITORY_ATTRIBUTES.put("username", List.of("uid"));

        PRINCIPAL = FunctionUtils.doUnchecked(() -> PrincipalFactoryUtils.newPrincipalFactory()
            .createPrincipal(UUID.randomUUID().toString(),
                Collections.singletonMap(MAIL, CollectionUtils.wrapList("final@school.com"))));

    }

    protected AbstractPrincipalAttributesRepository getPrincipalAttributesRepository(final String unit, final long duration) {
        return new CachingPrincipalAttributesRepository(unit, duration);
    }

    @Nested
    @SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        CachingPrincipalAttributesRepositoryTests.CacheTestConfiguration.class
    })
    public class MergingTests {
        private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "cachingPrincipalAttributesRepository.json");

        private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(true).build().toObjectMapper();

        @Autowired
        private ConfigurableApplicationContext applicationContext;


        @Test
        void verifySerializeACachingPrincipalAttributesRepositoryToJson() throws Throwable {

            val repositoryWritten = getPrincipalAttributesRepository(TimeUnit.MILLISECONDS.toString(), 1);
            repositoryWritten.setAttributeRepositoryIds(CollectionUtils.wrapSet("1", "2", "3"));
            MAPPER.writeValue(JSON_FILE, repositoryWritten);
            val repositoryRead = MAPPER.readValue(JSON_FILE, CachingPrincipalAttributesRepository.class);
            assertEquals(repositoryWritten, repositoryRead);
        }

        @Test
        void verifyMergingStrategyWithNoncollidingAttributeAdder() throws Throwable {
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .principal(PRINCIPAL)
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .build();
            try (val repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5)) {
                repository.setMergingStrategy(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD);
                repository.setAttributeRepositoryIds(Collections.singleton("Stub"));
                val repositoryAttributes = repository.getAttributes(context);
                assertTrue(repositoryAttributes.containsKey(MAIL));
                val emailValue = repositoryAttributes.get(MAIL).getFirst().toString();
                assertEquals("final@school.com", emailValue);
            }
        }

        @Test
        void verifyMergingStrategyWithReplacingAttributeAdder() throws Throwable {
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .principal(PRINCIPAL)
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .build();

            try (val repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5)) {
                repository.setAttributeRepositoryIds(Collections.singleton("Stub"));
                repository.setMergingStrategy(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE);
                val repositoryAttributes = repository.getAttributes(context);
                assertTrue(repositoryAttributes.containsKey(MAIL));
                val emailValue = repositoryAttributes.get(MAIL).getFirst().toString();
                assertEquals("final@example.com", emailValue, () -> "Attributes found are %s".formatted(repositoryAttributes));
            }
        }

        @Test
        void verifyMergingStrategyWithMultivaluedAttributeMerger() throws Throwable {
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .principal(PRINCIPAL)
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .build();

            try (val repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5)) {
                repository.setAttributeRepositoryIds(Collections.singleton("Stub"));
                repository.setMergingStrategy(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
                val repositoryAttributes = repository.getAttributes(context);
                val mailAttr = repositoryAttributes.get(MAIL);
                assertTrue(mailAttr.contains("final@example.com"), () -> "Attributes found are %s".formatted(repositoryAttributes));
                assertTrue(mailAttr.contains("final@school.com"), () -> "Attributes found are %s".formatted(repositoryAttributes));
            }
        }

    }

    @TestConfiguration
    static class CacheTestConfiguration {
        @Bean
        public IPersonAttributeDao attributeRepository() {
            val dao = mock(IPersonAttributeDao.class);
            val person = mock(IPersonAttributes.class);
            when(person.getName()).thenReturn("uid");
            when(person.getAttributes()).thenReturn(REPOSITORY_ATTRIBUTES);
            when(dao.getPerson(any(String.class), any(), any(IPersonAttributeDaoFilter.class))).thenReturn(person);
            when(dao.getPeople(any(Map.class), any(IPersonAttributeDaoFilter.class))).thenReturn(Set.of(person));
            when(dao.getId()).thenReturn(new String[]{"Stub"});
            return dao;
        }

        @Bean
        public PrincipalAttributesRepositoryCache principalAttributesRepositoryCache() {
            return new DefaultPrincipalAttributesRepositoryCache();
        }
    }
}

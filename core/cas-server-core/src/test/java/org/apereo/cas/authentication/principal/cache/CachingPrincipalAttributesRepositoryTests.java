package org.apereo.cas.authentication.principal.cache;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepositoryCache;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import java.nio.file.Files;
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
@ExtendWith(CasTestExtension.class)
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
                Map.of(MAIL, CollectionUtils.wrapList("final@school.com"))));

    }

    protected AbstractPrincipalAttributesRepository getPrincipalAttributesRepository(final String unit, final long duration) {
        return new CachingPrincipalAttributesRepository(unit, duration);
    }

    @Nested
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        AopAutoConfiguration.class,
        CacheTestConfiguration.class
    })
    class MergingTests {
        private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(true).build().toObjectMapper();

        @Autowired
        private ConfigurableApplicationContext applicationContext;


        @Test
        void verifySerializeACachingPrincipalAttributesRepositoryToJson() throws Throwable {
            val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
            val repositoryWritten = getPrincipalAttributesRepository(TimeUnit.MILLISECONDS.toString(), 1);
            repositoryWritten.setAttributeRepositoryIds(CollectionUtils.wrapSet("1", "2", "3"));
            MAPPER.writeValue(jsonFile, repositoryWritten);
            val repositoryRead = MAPPER.readValue(jsonFile, CachingPrincipalAttributesRepository.class);
            assertEquals(repositoryWritten, repositoryRead);
        }

        @Test
        void verifyMergingStrategyWithNoncollidingAttributeAdder() {
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .principal(PRINCIPAL)
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .build();
            val repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5);
            repository.setMergingStrategy(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD);
            repository.setAttributeRepositoryIds(Collections.singleton("Stub"));
            val repositoryAttributes = repository.getAttributes(context);
            assertTrue(repositoryAttributes.containsKey(MAIL));
            val emailValue = repositoryAttributes.get(MAIL).getFirst().toString();
            assertEquals("final@school.com", emailValue);
        }

        @Test
        void verifyMergingStrategyWithReplacingAttributeAdder() {
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .principal(PRINCIPAL)
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .build();

            val repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5);
            repository.setAttributeRepositoryIds(Collections.singleton("Stub"));
            repository.setMergingStrategy(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE);
            val repositoryAttributes = repository.getAttributes(context);
            assertTrue(repositoryAttributes.containsKey(MAIL));
            val emailValue = repositoryAttributes.get(MAIL).getFirst().toString();
            assertEquals("final@example.com", emailValue, () -> "Attributes found are %s".formatted(repositoryAttributes));

        }

        @Test
        void verifyMergingStrategyWithMultivaluedAttributeMerger() {
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .applicationContext(applicationContext)
                .principal(PRINCIPAL)
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .build();

            val repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5);
            repository.setAttributeRepositoryIds(Collections.singleton("Stub"));
            repository.setMergingStrategy(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
            val repositoryAttributes = repository.getAttributes(context);
            val mailAttr = repositoryAttributes.get(MAIL);
            assertTrue(mailAttr.contains("final@example.com"), () -> "Attributes found are %s".formatted(repositoryAttributes));
            assertTrue(mailAttr.contains("final@school.com"), () -> "Attributes found are %s".formatted(repositoryAttributes));
        }

    }

    @TestConfiguration(value = "CacheTestConfiguration", proxyBeanMethods = false)
    static class CacheTestConfiguration {
        @Bean
        public PersonAttributeDao attributeRepository() {
            val dao = mock(PersonAttributeDao.class);
            val person = mock(PersonAttributes.class);
            when(person.getName()).thenReturn("uid");
            when(person.getAttributes()).thenReturn(REPOSITORY_ATTRIBUTES);
            when(dao.getPerson(any(String.class), any(), any(PersonAttributeDaoFilter.class))).thenReturn(person);
            when(dao.getPeople(any(Map.class), any(PersonAttributeDaoFilter.class))).thenReturn(Set.of(person));
            when(dao.getId()).thenReturn(new String[]{"Stub"});
            return dao;
        }

        @Bean
        public PrincipalAttributesRepositoryCache principalAttributesRepositoryCache() {
            return new DefaultPrincipalAttributesRepositoryCache();
        }
    }
}

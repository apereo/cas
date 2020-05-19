package org.apereo.cas.authentication.principal.cache;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Handles tests for {@link CachingPrincipalAttributesRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CachingPrincipalAttributesRepositoryTests.CachingPrincipalAttributeRepositoryTestConfiguration.class
})
@DirtiesContext
@Tag("Simple")
public class CachingPrincipalAttributesRepositoryTests extends AbstractCachingPrincipalAttributesRepositoryTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "cachingPrincipalAttributesRepository.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @BeforeEach
    public void setup() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        CachingPrincipalAttributesRepository.getCacheInstanceFromApplicationContext().invalidateAll();
    }

    @Override
    protected AbstractPrincipalAttributesRepository getPrincipalAttributesRepository(final String unit, final long duration) {
        ApplicationContextProvider.registerBeanIntoApplicationContext(this.applicationContext, this.dao, "attributeRepository");
        return new CachingPrincipalAttributesRepository(unit, duration);
    }

    @Test
    @SneakyThrows
    public void verifySerializeACachingPrincipalAttributesRepositoryToJson() {
        val repositoryWritten = getPrincipalAttributesRepository(TimeUnit.MILLISECONDS.toString(), 1);
        repositoryWritten.setAttributeRepositoryIds(CollectionUtils.wrapSet("1", "2", "3"));
        MAPPER.writeValue(JSON_FILE, repositoryWritten);
        val repositoryRead = MAPPER.readValue(JSON_FILE, CachingPrincipalAttributesRepository.class);
        assertEquals(repositoryWritten, repositoryRead);
    }

    @TestConfiguration("CachingPrincipalAttributeRepositoryTestConfiguration")
    @Lazy(false)
    public static class CachingPrincipalAttributeRepositoryTestConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "principalAttributesRepositoryCache")
        public PrincipalAttributesRepositoryCache principalAttributesRepositoryCache() {
            return new PrincipalAttributesRepositoryCache();
        }
    }
}

package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ChainingPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.ServicesManager;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingPrincipalAttributesRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Attributes")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    ChainingPrincipalAttributesRepositoryTests.ChainingPrincipalAttributesRepositoryTestConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreUtilConfiguration.class
})
class ChainingPrincipalAttributesRepositoryTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val repo1 = new DefaultPrincipalAttributesRepository();
        repo1.setAttributeRepositoryIds(Set.of(UUID.randomUUID().toString()));
        val repo2 = new DefaultPrincipalAttributesRepository();
        repo2.setAttributeRepositoryIds(Set.of(UUID.randomUUID().toString()));

        val chain = new ChainingPrincipalAttributesRepository(List.of(repo1, repo2));

        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .build();

        val attributes = chain.getAttributes(context);
        assertNotNull(attributes);
        assertEquals(2, chain.getAttributeRepositoryIds().size());
        assertDoesNotThrow(() -> chain.update(CoreAuthenticationTestUtils.getPrincipal().getId(),
            CoreAuthenticationTestUtils.getPrincipal().getAttributes(), context));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ChainingPrincipalAttributesRepositoryTestConfiguration {
        @Bean
        public ServicesManager servicesManager() {
            return mock(ServicesManager.class);
        }
    }
}

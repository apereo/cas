package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
    ChainingPrincipalAttributesRepositoryTests.ChainingPrincipalAttributesRepositoryTestConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreUtilConfiguration.class
})
public class ChainingPrincipalAttributesRepositoryTests {
    @Test
    public void verifyOperation() {
        val repo1 = new DefaultPrincipalAttributesRepository();
        repo1.setAttributeRepositoryIds(Set.of(UUID.randomUUID().toString()));
        val repo2 = new DefaultPrincipalAttributesRepository();
        repo2.setAttributeRepositoryIds(Set.of(UUID.randomUUID().toString()));

        val chain = new ChainingPrincipalAttributesRepository(List.of(repo1, repo2));
        val attributes = chain.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertNotNull(attributes);
        assertEquals(2, chain.getAttributeRepositoryIds().size());
        assertDoesNotThrow(() -> chain.update(CoreAuthenticationTestUtils.getPrincipal().getId(),
            CoreAuthenticationTestUtils.getPrincipal().getAttributes(),
            CoreAuthenticationTestUtils.getRegisteredService()));
    }

    @TestConfiguration
    public static class ChainingPrincipalAttributesRepositoryTestConfiguration {
        @Bean
        public ServicesManager servicesManager() {
           return mock(ServicesManager.class);
        }
    }
}

package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ChainingPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
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
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    ChainingPrincipalAttributesRepositoryTests.ChainingPrincipalAttributesRepositoryTestConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class
})
class ChainingPrincipalAttributesRepositoryTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() {
        val repo1 = new DefaultPrincipalAttributesRepository();
        repo1.setAttributeRepositoryIds(Set.of(UUID.randomUUID().toString()));
        val repo2 = new DefaultPrincipalAttributesRepository();
        repo2.setAttributeRepositoryIds(Set.of(UUID.randomUUID().toString()));

        val chain = new ChainingPrincipalAttributesRepository(List.of(repo1, repo2));
        val context = RegisteredServiceAttributeReleasePolicyContext
            .builder()
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

    @TestConfiguration(value = "ChainingPrincipalAttributesRepositoryTestConfiguration", proxyBeanMethods = false)
    static class ChainingPrincipalAttributesRepositoryTestConfiguration {
        @Bean
        public ServicesManager servicesManager() {
            return mock(ServicesManager.class);
        }
    }
}

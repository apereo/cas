package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link JpaPersistenceUnitProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Hibernate")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    JpaPersistenceUnitProviderTests.JpaTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JpaPersistenceUnitProviderTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    @SetSystemProperty(key = CasRuntimeHintsRegistrar.SYSTEM_PROPERTY_SPRING_AOT_PROCESSING, value = "true")
    void verifyNullableEntityManager() throws Exception {
        val unitProvider = new DummyJpaPersistenceUnitProvider(applicationContext, null);
        val entityManager = unitProvider.recreateEntityManagerIfNecessary("DummyUnit");
        assertNotNull(entityManager);
        unitProvider.destroy();
    }


    @TestConfiguration(proxyBeanMethods = false)
    static class JpaTestConfiguration {
        @Bean
        public EntityManagerFactory testEntityManagerFactory() {
            val factory = mock(DummyEntityManagerFactory.class);
            when(factory.getPersistenceUnitName()).thenReturn("DummyUnit");
            when(factory.createEntityManager()).thenReturn(mock(EntityManager.class));
            return factory;
        }

        interface DummyEntityManagerFactory extends EntityManagerFactory, EntityManagerFactoryInfo {
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class DummyJpaPersistenceUnitProvider implements JpaPersistenceUnitProvider {
        private final ConfigurableApplicationContext applicationContext;
        private final EntityManager entityManager;
    }
}

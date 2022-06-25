package org.apereo.cas.util.spring.boot;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasFeatureEnabledConditionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Simple")
public class CasFeatureEnabledConditionTests {

    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AcceptableUsagePolicy,
        module = "feature3", enabledByDefault = false)
    @TestConfiguration(value = "CasFeatureModuleDisabledByDefaultTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasFeatureModuleDisabledByDefaultTestConfiguration {
        @Bean
        public String bean1() {
            return "Bean1";
        }
    }

    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AcceptableUsagePolicy, module = "feature1")
    @TestConfiguration(value = "CasFeatureModuleTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasFeatureModuleFeature1TestConfiguration {
        @Bean
        public String bean1() {
            return "Bean1";
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "CasFeatureModule.AcceptableUsagePolicy.feature1.enabled=true")
    @SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        CasFeatureModuleFeature1TestConfiguration.class
    })
    public class Feature1EnabledTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        public void verifyOperation() {
            assertTrue(applicationContext.containsBean("bean1"));
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "CasFeatureModule.AcceptableUsagePolicy.feature1.enabled=false")
    @SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        CasFeatureModuleFeature1TestConfiguration.class
    })
    public class Feature1DisabledTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        public void verifyOperation() {
            assertFalse(applicationContext.containsBean("bean1"));
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        CasFeatureModuleFeature1TestConfiguration.class
    })
    public class Feature1EnabledUndefinedTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        public void verifyOperation() {
            assertTrue(applicationContext.containsBean("bean1"));
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @SpringBootTest(classes = {
        RefreshAutoConfiguration.class,
        CasFeatureModuleDisabledByDefaultTestConfiguration.class
    })
    public class Feature3DisabledByDefaultTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        public void verifyOperation() {
            assertFalse(applicationContext.containsBean("bean1"));
        }
    }


}

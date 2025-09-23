package org.apereo.cas.util.spring.boot;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
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
@ExtendWith(CasTestExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class CasFeatureEnabledConditionTests {
    @ConditionalOnFeaturesEnabled({
        @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AcceptableUsagePolicy, module = "feature3"),
        @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AcceptableUsagePolicy, module = "feature4")
    })
    @TestConfiguration(value = "CasFeatureModuleMultipleConditionsTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasFeatureModuleMultipleConditionsTestConfiguration {
        @Bean
        public String beanMultiple() {
            return "beanMultiple";
        }
    }

    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AcceptableUsagePolicy,
        module = "feature3", enabledByDefault = false)
    @TestConfiguration(value = "CasFeatureModuleDisabledByDefaultTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasFeatureModuleDisabledByDefaultTestConfiguration {
        @Bean
        public String bean1() {
            return "Bean1";
        }
    }

    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AcceptableUsagePolicy, module = "feature1")
    @TestConfiguration(value = "CasFeatureModuleTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasFeatureModuleFeature1TestConfiguration {
        @Bean
        public String bean1() {
            return "Bean1";
        }
    }

    @ConditionalOnFeaturesEnabled({
        @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AcceptableUsagePolicy, module = "feature1"),
        @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProvider),
        @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ApacheTomcat)
    })
    @TestConfiguration(value = "CasFeatureModuleFeatureSelectedTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasFeatureModuleFeatureSelectedTestConfiguration {
        @Bean
        public String selectedBean() {
            return "BeanSelected";
        }
    }

    @Nested
    @TestPropertySource(properties = "CasFeatureModule.AcceptableUsagePolicy.feature1.enabled=true")
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        AopAutoConfiguration.class,
        CasFeatureModuleFeature1TestConfiguration.class
    })
    class Feature1EnabledTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyOperation() {
            assertTrue(applicationContext.containsBean("bean1"));
        }
    }

    @Nested
    @TestPropertySource(properties = "CasFeatureModule.AcceptableUsagePolicy.feature1.enabled=false")
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        AopAutoConfiguration.class,
        CasFeatureModuleFeature1TestConfiguration.class
    })
    class Feature1DisabledTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyOperation() {
            assertFalse(applicationContext.containsBean("bean1"));
        }
    }

    @Nested
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        AopAutoConfiguration.class,
        CasFeatureModuleFeature1TestConfiguration.class
    })
    class Feature1EnabledUndefinedTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyOperation() {
            assertTrue(applicationContext.containsBean("bean1"));
        }
    }

    @Nested
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        AopAutoConfiguration.class,
        CasFeatureModuleDisabledByDefaultTestConfiguration.class
    })
    class Feature3DisabledByDefaultTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyOperation() {
            assertFalse(applicationContext.containsBean("bean1"));
        }
    }

    @Nested
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        AopAutoConfiguration.class,
        CasFeatureModuleMultipleConditionsTestConfiguration.class
    })
    @TestPropertySource(properties = {
        "CasFeatureModule.AcceptableUsagePolicy.feature3.enabled=true",
        "CasFeatureModule.AcceptableUsagePolicy.feature4.enabled=true"
    })
    class FeatureMultipleConditionsTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyOperation() {
            assertTrue(applicationContext.containsBean("beanMultiple"));
        }
    }

    @Nested
    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        AopAutoConfiguration.class,
        CasFeatureModuleFeatureSelectedTestConfiguration.class
    },
        properties = CasFeatureEnabledCondition.PROPERTY_SELECTED_FEATURE_MODULES
            + "=CasFeatureModule.AcceptableUsagePolicy.feature1.enabled=true,"
            + "CasFeatureModule.SAMLIdentityProvider.enabled=true")
    class SelectedFeatureConditionsTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyOperation() {
            assertTrue(applicationContext.containsBean("selectedBean"));
            assertEquals(3, CasFeatureModule.FeatureCatalog.getRegisteredFeatures().size());
        }
    }

}

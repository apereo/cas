package org.apereo.cas.util.spring.boot;

import org.apereo.cas.util.InetAddressUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This is {@link ConditionalOnMatchingHostnameTests}.
 *
 * @author Hal Deadman
 * @since 6.4.0
 */
@Tag("Simple")
public class ConditionalOnMatchingHostnameTests {

    private static String HOSTNAME;

    private ConfigurableApplicationContext context;

    private ConfigurableEnvironment environment = new StandardEnvironment();

    @BeforeAll
    static void setup() {
        HOSTNAME = InetAddressUtils.getCasServerHostName();
    }

    @AfterEach
    void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    void regexMatch() {
        load(ConfigurationBeansDependOnHost.class, "hostname=.*");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void exactMatch() {
        load(ConfigurationBeansDependOnHost.class, "hostname=" + HOSTNAME);
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void blankMatch() {
        load(ConfigurationBeansDependOnHost.class, "hostname=");
        assertThat(this.context.containsBean("foo")).isTrue();
    }

    @Test
    void doesNotMatch() {
        load(ConfigurationBeansDependOnHost.class, "hostname=notright");
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void doesNotMatch2() {
        load(ConfigurationBeansDependOnHost.class, "hostname="+ HOSTNAME + "2");
        assertThat(this.context.containsBean("foo")).isFalse();
    }

    @Test
    void exactMatchAndPropertyTrue() {
        load(ConfigurationBeansDependOnHostAndProperty.class, "hostname=" + HOSTNAME, "someproperty=true");
        assertThat(this.context.containsBean("bar")).isTrue();
    }

    @Test
    void exactMatchAndPropertyFalse() {
        load(ConfigurationBeansDependOnHostAndProperty.class, "hostname=" + HOSTNAME, "someproperty=false");
        assertThat(this.context.containsBean("bar")).isFalse();
    }

    @Test
    void hostnamePropertyNotSet() {
        load(ConfigurationBeansDependOnHostAndProperty.class, "someproperty=true");
        assertThat(this.context.containsBean("bar")).isTrue();
    }


    @TestConfiguration("ConfigurationBeansDependOnHost")
    @ConditionalOnMatchingHostname(name = "hostname")
    static class ConfigurationBeansDependOnHost {

        @Bean
        public String foo() {
            return "foo";
        }

    }

    @TestConfiguration("ConfigurationBeansDependOnHostAndProperty")
    @ConditionalOnProperty(name = "someproperty", havingValue="true")
    @ConditionalOnMatchingHostname(name = "hostname")
    static class ConfigurationBeansDependOnHostAndProperty {

        @Bean
        public String bar() {
            return "bar";
        }

    }

    private void load(final Class<?> config, final String... environment) {
        TestPropertyValues.of(environment).applyTo(this.environment);
        this.context = new SpringApplicationBuilder(config).environment(this.environment).web(WebApplicationType.NONE)
            .run();
    }
}

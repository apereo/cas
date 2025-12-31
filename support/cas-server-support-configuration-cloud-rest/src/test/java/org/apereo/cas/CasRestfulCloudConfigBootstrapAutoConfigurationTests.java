package org.apereo.cas;

import module java.base;
import org.apereo.cas.config.CasRestfulCloudConfigBootstrapAutoConfiguration;
import org.apereo.cas.config.RestfulPropertySource;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasRestfulCloudConfigBootstrapAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasRestfulCloudConfigBootstrapAutoConfiguration.class,
    properties = RestfulPropertySource.CAS_CONFIGURATION_PREFIX + ".url=http://localhost:56565/casconfig")
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 56565)
class CasRestfulCloudConfigBootstrapAutoConfigurationTests {
    private static final String STATIC_AUTHN_USERS = "casuser::P@$$w0rd,admin::P@$$w0rd";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableEnvironment environment;

    @Test
    void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
        val propertySource = environment.getPropertySources()
            .stream()
            .filter(source -> source instanceof BootstrapPropertySource<?>)
            .map(BootstrapPropertySource.class::cast)
            .map(BootstrapPropertySource::getDelegate)
            .filter(MutablePropertySource.class::isInstance)
            .map(MutablePropertySource.class::cast)
            .findFirst()
            .orElseThrow();
        propertySource.setProperty("cas.server.prefix", "https://example.org/cas");
        assertTrue(List.of(propertySource.getPropertyNames()).contains("cas.server.prefix"));
        propertySource.removeProperty("cas.server.prefix");
        propertySource.removeAll();
        assertEquals(0, propertySource.getPropertyNames().length);
    }
}

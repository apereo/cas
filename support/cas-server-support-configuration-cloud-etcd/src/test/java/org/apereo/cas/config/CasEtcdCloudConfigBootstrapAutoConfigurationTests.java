package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEtcdCloudConfigBootstrapAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasEtcdCloudConfigBootstrapAutoConfiguration.class)
@Tag("CasConfiguration")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 2379)
class CasEtcdCloudConfigBootstrapAutoConfigurationTests {
    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableEnvironment environment;

    static {
        System.setProperty(CasEtcdCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + ".endpoints", "http://localhost:2379");
        try (val client = Client.builder().endpoints("http://localhost:2379").build()) {
            val kvClient = client.getKVClient();
            kvClient.put(ByteSequence.from("/cas/config/default/cas.authn.accept.users", StandardCharsets.UTF_8), ByteSequence.from(STATIC_AUTHN_USERS, StandardCharsets.UTF_8)).get();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

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
        propertySource.setProperty("cas.server.prefix", "https://sso.example.org/cas");
        assertEquals("https://sso.example.org/cas", propertySource.getProperty("cas.server.prefix"));
        propertySource.removeProperty("cas.server.prefix");
        assertNull(propertySource.getProperty("cas.server.prefix"));
        propertySource.removeAll();
        assertEquals(0, propertySource.getPropertyNames().length);
    }

}

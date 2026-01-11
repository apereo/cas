package org.apereo.cas;

import module java.base;
import org.apereo.cas.config.CasMongoDbCloudConfigBootstrapAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasMongoDbCloudConfigBootstrapAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("MongoDb")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 27017)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTestAutoConfigurations
@Import(CasMongoDbCloudConfigBootstrapAutoConfigurationTests.MongoDbConfigurationTestConfiguration.class)
@SpringBootTest(classes = CasMongoDbCloudConfigBootstrapAutoConfiguration.class)
class CasMongoDbCloudConfigBootstrapAutoConfigurationTests {
    static final String MONGODB_URI = "mongodb://root:secret@localhost:27017/admin";

    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";
    private static final String CAS_SERVER_PREFIX = "https://cas.sso.example.org/cas";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    @Qualifier("casServerPrefixSupplier")
    private Supplier<String> casServerPrefixSupplier;

    static {
        System.setProperty(CasMongoDbCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_MONGODB_URI,
            CasMongoDbCloudConfigBootstrapAutoConfigurationTests.MONGODB_URI);
    }

    @BeforeAll
    public static void initialize() {
        val template = new MongoTemplate(new SimpleMongoClientDatabaseFactory(MONGODB_URI));
        template.dropCollection(MongoDbProperty.class.getSimpleName());
        template.createCollection(MongoDbProperty.class.getSimpleName());

        var object = new MongoDbProperty();
        object.setId(UUID.randomUUID().toString());
        object.setName("cas.authn.accept.users");
        object.setValue(STATIC_AUTHN_USERS);
        template.insert(object, MongoDbProperty.class.getSimpleName());

        object = new MongoDbProperty();
        object.setId(UUID.randomUUID().toString());
        object.setName("cas.server.prefix");
        object.setValue(CAS_SERVER_PREFIX);
        template.insert(object, MongoDbProperty.class.getSimpleName());
    }

    @Test
    void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
        assertEquals(CAS_SERVER_PREFIX, casProperties.getServer().getPrefix());
        assertEquals(CAS_SERVER_PREFIX, casServerPrefixSupplier.get());

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
        val prefix = environment.getProperty("cas.server.prefix");
        assertEquals("https://example.org/cas", prefix);
        propertySource.removeProperty("cas.server.prefix");
        val prefixAfterDelete = environment.getProperty("cas.server.prefix");
        assertNull(prefixAfterDelete);
        assertFalse(propertySource.getPropertyNames("cas.authn.accept.*").isEmpty());
        propertySource.removeAll();
        assertEquals(0, propertySource.getPropertyNames().length);
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class MongoDbConfigurationTestConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Supplier<String> casServerPrefixSupplier(final CasConfigurationProperties casProperties) {
            return () -> casProperties.getServer().getPrefix();
        }
    }
}

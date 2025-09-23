package org.apereo.cas;

import org.apereo.cas.config.CasMongoDbCloudConfigBootstrapAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import java.util.UUID;
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
@SpringBootTest(classes = CasMongoDbCloudConfigBootstrapAutoConfiguration.class,
    properties = "cas.spring.cloud.mongo.uri=" + CasMongoDbCloudConfigBootstrapAutoConfigurationTests.MONGODB_URI)
class CasMongoDbCloudConfigBootstrapAutoConfigurationTests {
    static final String MONGODB_URI = "mongodb://root:secret@localhost:27017/admin";

    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() {
        val template = new MongoTemplate(new SimpleMongoClientDatabaseFactory(MONGODB_URI));
        template.dropCollection(MongoDbProperty.class.getSimpleName());
        template.createCollection(MongoDbProperty.class.getSimpleName());

        val object = new MongoDbProperty();
        object.setId(UUID.randomUUID().toString());
        object.setName("cas.authn.accept.users");
        object.setValue(STATIC_AUTHN_USERS);
        template.insert(object, MongoDbProperty.class.getSimpleName());
    }

    @Test
    void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
    }
}

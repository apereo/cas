package org.apereo.cas;

import org.apereo.cas.config.MongoDbCloudConfigBootstrapConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbCloudConfigBootstrapConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("MongoDb")
@EnabledIfPortOpen(port = 27017)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    MongoDbCloudConfigBootstrapConfiguration.class
},
    properties = "cas.spring.cloud.mongo.uri=" + MongoDbCloudConfigBootstrapConfigurationTests.MONGODB_URI)
public class MongoDbCloudConfigBootstrapConfigurationTests {
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
    public void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
    }
}

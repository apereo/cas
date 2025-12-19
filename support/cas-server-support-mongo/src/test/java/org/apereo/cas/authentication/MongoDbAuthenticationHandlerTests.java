package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMongoAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link MongoDbAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasMongoAuthenticationAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAutoConfiguration.class
}, properties = {
    "cas.authn.mongo.client-uri=mongodb://root:secret@localhost:27017/admin",
    "cas.authn.mongo.collection=users",
    "cas.authn.mongo.database-name=cas",
    "cas.authn.mongo.attributes=loc,state",
    "cas.authn.mongo.username-attribute=username",
    "cas.authn.mongo.password-attribute=password"
})
@EnableScheduling
@EnabledIfListeningOnPort(port = 27017)
@Tag("MongoDb")
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class MongoDbAuthenticationHandlerTests {
    @Autowired
    @Qualifier("mongoAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeEach
    void initialize() {
        val mongo = casProperties.getAuthn().getMongo();
        val factory = new MongoDbConnectionFactory();
        try (val mongoClient = factory.buildMongoDbClient(mongo)) {
            val database = mongoClient.getDatabase(mongo.getDatabaseName());
            database.drop();
            val col = database.getCollection(mongo.getCollection());

            var account = new Document();
            account.append(mongo.getUsernameAttribute(), "u1");
            account.append(mongo.getPasswordAttribute(), "p1");
            account.append("loc", "Apereo");
            account.append("state", "California");
            col.insertOne(account);

            account = new Document();
            account.append(mongo.getUsernameAttribute(), "userPlain");
            col.insertOne(account);
        }
    }

    @Test
    void verifyAuthentication() throws Throwable {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("u1", "p1");
        val result = authenticationHandler.authenticate(creds, mock(Service.class));
        assertEquals("u1", result.getPrincipal().getId());
        val attributes = result.getPrincipal().getAttributes();
        assertTrue(attributes.containsKey("loc"));
        assertTrue(attributes.containsKey("state"));
    }

    @Test
    void verifyAuthenticationFails() {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("unknown", "p1");
        assertThrows(AccountNotFoundException.class, () -> authenticationHandler.authenticate(creds, mock(Service.class)));
    }

    @Test
    void verifyNoPsw() {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("userPlain", "p1");
        assertThrows(FailedLoginException.class, () -> authenticationHandler.authenticate(creds, mock(Service.class)));
    }

    @Test
    void verifyBadPsw() {
        val creds = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("u1", "other");
        assertThrows(FailedLoginException.class, () -> authenticationHandler.authenticate(creds, mock(Service.class)));
    }
}

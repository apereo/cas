package org.apereo.cas.authentication;

import org.apereo.cas.authentication.config.CasMongoAuthenticationConfiguration;
import org.apereo.cas.category.MongoDbCategory;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.val;
import org.bson.Document;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.Assert.*;

/**
 * Test cases for {@link MongoDbAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@SpringBootTest(classes = {
    CasMongoAuthenticationConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    RefreshAutoConfiguration.class})
@EnableScheduling
@TestPropertySource(properties = {
    "cas.authn.mongo.collection=users",
    "cas.authn.mongo.databaseName=cas",
    "cas.authn.mongo.clientUri=mongodb://root:secret@localhost:27017/admin",
    "cas.authn.mongo.attributes=loc,state",
    "cas.authn.mongo.usernameAttribute=username",
    "cas.authn.mongo.passwordAttribute=password",
    "cas.authn.pac4j.typedIdUsed=false"
})
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
@Category(MongoDbCategory.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MongoDbAuthenticationHandlerTests {

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("mongoAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeEach
    public void initialize() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));

        val mongo = casProperties.getAuthn().getMongo();
        val mongoClient = MongoDbConnectionFactory.buildMongoDbClient(mongo);
        mongoClient.dropDatabase(mongo.getDatabaseName());
        val database = mongoClient.getDatabase(mongo.getDatabaseName());
        val col = database.getCollection(mongo.getCollection());
        val account = new Document();
        account.append(mongo.getUsernameAttribute(), "u1");
        account.append(mongo.getPasswordAttribute(), "p1");
        account.append("loc", "Apereo");
        account.append("state", "California");
        col.insertOne(account);
    }

    @Test
    public void verifyAuthentication() throws Exception {
        val result = this.authenticationHandler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("u1", "p1"));
        assertEquals("u1", result.getPrincipal().getId());
        val attributes = result.getPrincipal().getAttributes();
        assertTrue(attributes.containsKey("loc"));
        assertTrue(attributes.containsKey("state"));
    }
}

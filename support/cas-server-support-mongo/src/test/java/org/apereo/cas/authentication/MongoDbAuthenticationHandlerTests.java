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
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pac4j.mongo.profile.MongoProfile;
import org.pac4j.mongo.profile.service.MongoProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.Assert.*;

/**
 * Test cases for {@link MongoDbAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Category(MongoDbCategory.class)
@SpringBootTest(
    classes = {CasMongoAuthenticationConfiguration.class,
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
    "cas.authn.mongo.collectionName=users",
    "cas.authn.mongo.mongoHostUri=mongodb://root:secret@localhost:27017/users",
    "cas.authn.mongo.attributes=loc,state",
    "cas.authn.pac4j.typedIdUsed=false"
    })
@Slf4j
public class MongoDbAuthenticationHandlerTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("mongoAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("mongoAuthenticatorProfileService")
    private MongoProfileService mongoProfileService;

    @Before
    public void initialize() {
        val profile = new MongoProfile();
        profile.setId("CASUSER");
        profile.addAttributes(CollectionUtils.wrap("loc", "test", "state", "passing", "username", "u1"));
        LOGGER.debug("using: [{}/{}]", mongoProfileService.getUsersDatabase(), mongoProfileService.getUsersCollection());
        mongoProfileService.create(profile, "p1");

        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));
    }

    @Test
    public void verifyAuthentication() throws Exception {
        val result = this.authenticationHandler.authenticate(CoreAuthenticationTestUtils
            .getCredentialsWithDifferentUsernameAndPassword("u1", "p1"));
        assertEquals("u1", result.getPrincipal().getId());
        assertTrue(result.getPrincipal().getAttributes().containsKey("loc"));
        assertTrue(result.getPrincipal().getAttributes().containsKey("state"));
    }
}

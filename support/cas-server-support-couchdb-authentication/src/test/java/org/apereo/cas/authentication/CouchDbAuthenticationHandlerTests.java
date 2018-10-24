package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.category.CouchDbCategory;
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
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CouchDbAuthenticationConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pac4j.couch.profile.CouchProfile;
import org.pac4j.couch.profile.service.CouchProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.GeneralSecurityException;

import static org.junit.Assert.*;

/**
 * This is {@link CouchDbAuthenticationHandlerTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@SpringBootTest(
    classes = {
        CasCouchDbCoreConfiguration.class,
        CouchDbAuthenticationConfiguration.class,
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
        RefreshAutoConfiguration.class
    })
@TestPropertySource(properties = {
    "cas.authn.couchDb.dbName=authentication",
    "cas.authn.couchDb.attributes=loc,state",
    "cas.authn.couchDb.usernameAttribute=username",
    "cas.authn.couchDb.passwordAttribute=password",
    "cas.authn.couchDb.username=cas",
    "cas.authn.couchdb.password=password",
    "cas.authn.pac4j.typedIdUsed=false"
})
@Category(CouchDbCategory.class)
public class CouchDbAuthenticationHandlerTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("authenticationCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("authenticationCouchDbRepository")
    private ProfileCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("couchDbAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("couchDbPrincipalFactory")
    private PrincipalFactory principalFactory;

    @Autowired
    @Qualifier("couchDbAuthenticatorProfileService")
    private CouchProfileService profileService;

    @BeforeEach
    public void setUp() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));
        val profile = new CouchProfile();
        profile.build("u1", CollectionUtils.wrap("loc", "Chicago", "state", "Illinois", "username", "u1"));
        profileService.create(profile, "p1");
    }

    @AfterEach
    public void tearDown() {
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }

    @Test
    public void verifyAuthentication() throws GeneralSecurityException, PreventedException {
        val result = this.authenticationHandler.authenticate(CoreAuthenticationTestUtils
            .getCredentialsWithDifferentUsernameAndPassword("u1", "p1"));
        assertEquals("u1", result.getPrincipal().getId());
        assertEquals("Chicago", result.getPrincipal().getAttributes().get("loc"));
        assertEquals("Illinois", result.getPrincipal().getAttributes().get("state"));
    }
}

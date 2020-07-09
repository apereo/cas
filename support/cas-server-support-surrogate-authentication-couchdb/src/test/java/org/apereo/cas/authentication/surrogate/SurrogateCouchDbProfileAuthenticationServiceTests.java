package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.SurrogateCouchDbAuthenticationServiceConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.CouchDbProfileDocument;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link SurrogateCouchDbProfileAuthenticationServiceTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Tag("CouchDb")
@SpringBootTest(classes = {
    CasCouchDbCoreConfiguration.class,
    SurrogateCouchDbAuthenticationServiceConfiguration.class,
    BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.surrogate.couch-db.db-name=surrogate_profile",
    "cas.authn.surrogate.couch-db.profile-based=true",
    "cas.authn.surrogate.couch-db.surrogate-principals-attribute=surrogateFor",
    "cas.authn.surrogate.couch-db.username=cas",
    "cas.authn.surrogate.couch-db.caching=false",
    "cas.authn.surrogate.couch-db.password=password"
})
@Getter
@EnabledIfPortOpen(port = 5984)
public class SurrogateCouchDbProfileAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {

    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private SurrogateAuthenticationService service;

    @Autowired
    @Qualifier("surrogateCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("surrogateAuthorizationProfileCouchDbRepository")
    private ProfileCouchDbRepository repository;

    @BeforeEach
    public void setUp() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        repository.initStandardDesignDocument();

        val profile = new CouchDbProfileDocument();
        profile.setUsername("casuser");
        profile.setAttribute("surrogateFor", CollectionUtils.wrapList("banderson"));
        repository.add(profile);
    }

    @AfterEach
    public void tearDown() {
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }
}

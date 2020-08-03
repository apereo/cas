package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.SurrogateCouchDbAuthenticationServiceConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.surrogate.CouchDbSurrogateAuthorization;
import org.apereo.cas.couchdb.surrogate.SurrogateAuthorizationCouchDbRepository;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link SurrogateCouchDbAuthenticationTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Tag("CouchDb")
@SpringBootTest(classes = {
    SurrogateCouchDbAuthenticationServiceConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class
    }, properties = {
        "cas.authn.surrogate.couch-db.username=cas",
        "cas.authn.surrogate.couch-db.caching=false",
        "cas.authn.surrogate.couch-db.password=password"
    })
@Getter
@EnabledIfPortOpen(port = 5984)
public class SurrogateCouchDbAuthenticationTests extends BaseSurrogateAuthenticationServiceTests {

    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private SurrogateAuthenticationService service;

    @Autowired
    @Qualifier("surrogateCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("surrogateAuthorizationCouchDbRepository")
    private SurrogateAuthorizationCouchDbRepository repository;

    @BeforeEach
    public void setUp() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        repository.initStandardDesignDocument();
        repository.add(new CouchDbSurrogateAuthorization("banderson", "casuser"));
    }

    @AfterEach
    public void tearDown() {
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }
}

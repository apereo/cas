package org.apereo.cas.aup;

import org.apereo.cas.config.CasAcceptableUsagePolicyCouchDbConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchDbAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Import({
    CasCouchDbCoreConfiguration.class,
    CasAcceptableUsagePolicyCouchDbConfiguration.class,
    BaseAcceptableUsagePolicyRepositoryTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = {
    "cas.acceptable-usage-policy.couch-db.asynchronous=false",
    "cas.acceptable-usage-policy.couch-db.username=cas",
    "cas.acceptable-usage-policy.couch-db.password=password"
})
@Tag("CouchDb")
@Getter
@EnabledIfPortOpen(port = 5984)
public class CouchDbAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {

    @Autowired
    @Qualifier("aupCouchDbFactory")
    private CouchDbConnectorFactory aupCouchDbFactory;

    @Autowired
    @Qualifier("aupCouchDbRepository")
    private ProfileCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    private AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @BeforeEach
    public void setUp() {
        aupCouchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(aupCouchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @AfterEach
    public void tearDown() {
        aupCouchDbFactory.getCouchDbInstance().deleteDatabase(aupCouchDbFactory.getCouchDbConnector().getDatabaseName());
    }

    @Test
    public void verifyOperation() {
        assertNotNull(acceptableUsagePolicyRepository);
        verifyRepositoryAction("casuser",
            CollectionUtils.wrap("aupAccepted", List.of("false"), "email", List.of("CASuser@example.org")));
    }
}

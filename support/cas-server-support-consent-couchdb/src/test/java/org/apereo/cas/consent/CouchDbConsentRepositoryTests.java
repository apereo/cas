package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentCouchDbConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.couchdb.consent.ConsentDecisionCouchDbRepository;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CouchDbConsentRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    CasCouchDbCoreConfiguration.class,
    CasConsentCouchDbConfiguration.class,
    BaseConsentRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.consent.couch-db.username=cas",
        "cas.consent.couch-db.password=password",
        "cas.consent.couch-db.caching=false"
    })
@Tag("CouchDb")
@Getter
@EnabledIfPortOpen(port = 5984)
public class CouchDbConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier("consentCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("consentCouchDbRepository")
    private ConsentDecisionCouchDbRepository couchDbRepository;

    @Autowired
    @Qualifier("consentRepository")
    private ConsentRepository repository;

    @BeforeEach
    public void setUp() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @AfterEach
    public void tearDown() {
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }

    @Test
    public void verifyFailsOperation() {
        assertTrue(couchDbRepository.findConsentDecision("unknown", "unknown").isEmpty());
        val decision = BUILDER.build(SVC, REG_SVC, "casuser", ATTR);
        val mockRepo = mock(ConsentDecisionCouchDbRepository.class);
        when(mockRepo.findFirstConsentDecision(any(ConsentDecision.class))).thenThrow(new RuntimeException());
        when(mockRepo.findByPrincipalAndId(anyString(), anyLong())).thenThrow(new RuntimeException());
        assertNull(new CouchDbConsentRepository(mockRepo).storeConsentDecision(decision));
        assertFalse(new CouchDbConsentRepository(mockRepo).deleteConsentDecision(1, "casuser"));
    }

}

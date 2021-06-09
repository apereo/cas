package org.apereo.cas.aup;

import org.apereo.cas.config.CasAcceptableUsagePolicyCouchDbConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

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

    @Override
    public boolean hasLiveUpdates() {
        return true;
    }

    @Test
    public void verifyOperation() {
        assertNotNull(acceptableUsagePolicyRepository);
        val attributes = CollectionUtils.<String, List<Object>>wrap("aupAccepted", List.of("false"),
            "email", List.of("CASuser@example.org"));
        verifyRepositoryAction("casuser", attributes);

        val c = getCredential("casuser");
        val context = getRequestContext("casuser", attributes, c);
        acceptableUsagePolicyRepository.submit(context);
        assertTrue(getAcceptableUsagePolicyRepository().verify(context).isAccepted());

        val principal = RegisteredServiceTestUtils.getPrincipal("casuser", Map.of("aupAccepted", List.of("true")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);
        WebUtils.putAuthentication(authentication, context);

        val tgt = new MockTicketGrantingTicket(authentication);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        ticketRegistry.addTicket(tgt);
        
        assertTrue(getAcceptableUsagePolicyRepository().verify(context).isAccepted());
    }
}

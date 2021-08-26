package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentCouchDbConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.couchdb.consent.CouchDbConsentDecision;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchDbConsentDecisionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
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
public class CouchDbConsentDecisionTests {
    @Test
    public void verifyOperation() {
        val consent = new ConsentDecision();
        consent.setAttributes("attributes");
        consent.setCreatedDate(LocalDateTime.now(ZoneId.systemDefault()));
        consent.setOptions(ConsentReminderOptions.ATTRIBUTE_NAME);
        consent.setPrincipal("casuser");
        consent.setReminder(10L);
        consent.setReminderTimeUnit(ChronoUnit.MONTHS);
        consent.setService("service");

        val decision = new CouchDbConsentDecision(consent);
        assertEquals(decision, decision.copyDetailsFrom(consent));
    }

}

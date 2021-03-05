package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.U2FConfiguration;
import org.apereo.cas.config.U2FCouchDbConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.u2f.U2FDeviceRegistrationCouchDbRepository;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link U2FCouchDbDeviceRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Tag("CouchDb")
@SpringBootTest(classes = {
    U2FCouchDbConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    AopAutoConfiguration.class,
    U2FConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.authn.mfa.u2f.couch-db.asynchronous=false",
        "cas.authn.mfa.u2f.couch-db.caching=false",
        "cas.authn.mfa.u2f.couch-db.username=cas",
        "cas.authn.mfa.u2f.couch-db.password=password"
    })
@Getter
@EnabledIfPortOpen(port = 5984)
public class U2FCouchDbDeviceRepositoryTests extends AbstractU2FDeviceRepositoryTests {
    @Autowired
    @Qualifier("u2fCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FCouchDbDeviceRepository deviceRepository;

    @Autowired
    @Qualifier("couchDbU2fDeviceRegistrationRepository")
    private U2FDeviceRegistrationCouchDbRepository couchDbRepository;

    @BeforeEach
    @Override
    public void setUp() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
    }

    @AfterEach
    public void tearDown() {
        deviceRepository.removeAll();
        couchDbRepository.deleteAll();
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }

    @Test
    public void verifyOperation() {
        assertNotNull(deviceRepository);
    }
}

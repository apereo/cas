package org.apereo.cas.gauth.credential;

import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.GoogleAuthenticatorCouchDbConfiguration;
import org.apereo.cas.couchdb.core.CouchDbConnectorFactory;
import org.apereo.cas.couchdb.gauth.credential.GoogleAuthenticatorAccountCouchDbRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link CouchDbGoogleAuthenticatorTokenCredentialRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Tag("CouchDb")
@SpringBootTest(classes = {
    CasCouchDbCoreConfiguration.class,
    GoogleAuthenticatorCouchDbConfiguration.class,
    BaseOneTimeTokenCredentialRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.mfa.gauth.crypto.enabled=false",
        "cas.authn.mfa.gauth.couch-db.username=cas",
        "cas.authn.mfa.gauth.couch-db.caching=false",
        "cas.authn.mfa.gauth.couch-db.db-name=gauth_credential",
        "cas.authn.mfa.gauth.couch-db.password=password"
    })
@Getter
@EnabledIfPortOpen(port = 5984)
public class CouchDbGoogleAuthenticatorTokenCredentialRepositoryTests extends BaseOneTimeTokenCredentialRepositoryTests {

    @Autowired
    @Qualifier("oneTimeTokenAccountCouchDbFactory")
    private CouchDbConnectorFactory couchDbFactory;

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository registry;

    @Autowired
    @Qualifier("couchDbOneTimeTokenAccountRepository")
    private GoogleAuthenticatorAccountCouchDbRepository couchDbRepository;

    @BeforeEach
    @Override
    public void initialize() {
        couchDbFactory.getCouchDbInstance().createDatabaseIfNotExists(couchDbFactory.getCouchDbConnector().getDatabaseName());
        couchDbRepository.initStandardDesignDocument();
        registry.deleteAll();
        super.initialize();
    }

    @AfterEach
    @Override
    public void afterEach() {
        super.afterEach();
        registry.deleteAll();
        couchDbFactory.getCouchDbInstance().deleteDatabase(couchDbFactory.getCouchDbConnector().getDatabaseName());
    }
}

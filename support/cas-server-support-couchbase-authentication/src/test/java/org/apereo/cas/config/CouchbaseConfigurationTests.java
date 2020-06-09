package org.apereo.cas.config;

import org.apereo.cas.AbstractCouchbaseTests;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchbaseConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Couchbase")
@EnabledIfPortOpen(port = 8091)
@SpringBootTest(classes = AbstractCouchbaseTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.couchbase.cluster-username=admin",
        "cas.authn.couchbase.cluster-password=password",
        "cas.authn.couchbase.bucket=testbucket",

        "cas.authn.attribute-repository.couchbase.cluster-password=password",
        "cas.authn.attribute-repository.couchbase.cluster-username=admin",
        "cas.authn.attribute-repository.couchbase.bucket=testbucket",
        "cas.authn.attribute-repository.couchbase.username-attribute=username"
    })
public class CouchbaseConfigurationTests {
    @Autowired
    @Qualifier("couchbaseAttributeRepositoryPlanConfigurer")
    private PersonDirectoryAttributeRepositoryPlanConfigurer couchbaseAttributeRepositoryPlanConfigurer;

    @Autowired
    @Qualifier("couchbaseAuthenticationHandler")
    private AuthenticationHandler couchbaseAuthenticationHandler;

    @Test
    public void verifyOperation() {
        assertNotNull(couchbaseAttributeRepositoryPlanConfigurer);
        assertNotNull(couchbaseAuthenticationHandler);
    }
}

package org.apereo.cas.authentication;

import org.apereo.cas.AbstractCouchbaseTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchbasePersonAttributeDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchbasePersonAttributeDaoTests {
    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Test
    public void verifyAttributes() {
        val person = attributeRepository.getPerson("casuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);
        val attributes = person.getAttributes();
        assertTrue(attributes.containsKey("firstname"));
        assertTrue(attributes.containsKey("lastname"));
        assertEquals("casuser", person.getName());
    }
}

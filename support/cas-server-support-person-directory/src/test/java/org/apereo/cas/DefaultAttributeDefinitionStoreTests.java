package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAttributeDefinitionStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    CasPersonDirectoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.authn.attributeRepository.stub.attributes.uid=cas-user-id",
        "cas.authn.attributeRepository.stub.attributes.givenName=cas-given-name",
        "cas.authn.attributeRepository.stub.attributes.eppn=casuser",
        "cas.authn.attributeRepository.stub.attributes.mismatchedAttributeKey=someValue",

        "cas.server.scope=cas.org",

        "cas.person-directory.attribute-definition-store.json.location=classpath:/basic-attribute-definitions.json"
    })
public class DefaultAttributeDefinitionStoreTests {

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Test
    public void verifyReturnAll() {
        val person = attributeRepository.getPerson("casuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);

        val policy = new ReturnAllAttributeReleasePolicy();
        val attributes = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal(person.getAttributes()),
            CoreAuthenticationTestUtils.getService(), CoreAuthenticationTestUtils.getRegisteredService());
        assertNotNull(attributes);
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("givenName"));
        assertTrue(attributes.containsKey("urn:oid:1.3.6.1.4.1.5923.1.1.1.6"));
        assertTrue(List.class.cast(attributes.get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")).contains("cas-user-id@cas.org"));
    }

    @Test
    public void verifyMismatchedKeyReturnAll() {
        val person = attributeRepository.getPerson("casuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);

        val policy = new ReturnAllAttributeReleasePolicy();
        val attributes = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal(person.getAttributes()),
            CoreAuthenticationTestUtils.getService(), CoreAuthenticationTestUtils.getRegisteredService());
        assertNotNull(attributes);
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("interesting-attribute"));
        assertTrue(List.class.cast(attributes.get("interesting-attribute")).contains("cas-given-name@cas.org"));
    }
}

package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalAttributeRepositoryFetcherCascadeTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.authn.attribute-repository.stub.attributes.uid=cas",
    "cas.authn.attribute-repository.stub.attributes.givenName=apereo-cas",
    "cas.authn.attribute-repository.stub.attributes.eppn=casuser",

    "cas.authn.attribute-repository.json[0].location=classpath:/json-attribute-repository.json",
    "cas.authn.attribute-repository.json[0].order=1",

    "cas.authn.attribute-repository.groovy[0].location=classpath:/GroovyAttributeRepository.groovy",
    "cas.authn.attribute-repository.groovy[0].order=2",

    "cas.authn.attribute-repository.core.aggregation=CASCADE",
    "cas.authn.attribute-repository.core.merger=MULTIVALUED"
})
@Tag("Attributes")
public class PrincipalAttributeRepositoryFetcherCascadeTests {
    @Autowired
    @Qualifier("aggregatingAttributeRepository")
    private IPersonAttributeDao aggregatingAttributeRepository;

    @Test
    public void verifyOperation() {
        val attributes = PrincipalAttributeRepositoryFetcher.builder()
            .attributeRepository(aggregatingAttributeRepository)
            .principalId("casuser")
            .currentPrincipal(CoreAuthenticationTestUtils.getPrincipal("current-cas"))
            .build()
            .retrieve();
        assertNotNull(attributes);
    }
}

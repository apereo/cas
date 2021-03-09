package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalAttributeRepositoryFetcherCascadeTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class, properties = {
    "cas.authn.attribute-repository.json[0].location=classpath:/json-attribute-repository.json",
    "cas.authn.attribute-repository.json[0].order=1",

    "cas.authn.attribute-repository.groovy[0].location=classpath:/GroovyAttributeRepository.groovy",
    "cas.authn.attribute-repository.groovy[0].order=2",
    "cas.authn.attribute-repository.core.require-all-repository-sources=true"
})
@Tag("Attributes")
public class PrincipalAttributeRepositoryFetcherTests {
    @Autowired
    @Qualifier("aggregatingAttributeRepository")
    private IPersonAttributeDao aggregatingAttributeRepository;

    @Test
    public void verifyOperation() {
        val attributes = PrincipalAttributeRepositoryFetcher.builder()
            .attributeRepository(aggregatingAttributeRepository)
            .principalId("casuser-whatever")
            .currentPrincipal(CoreAuthenticationTestUtils.getPrincipal("current-cas"))
            .build()
            .retrieve();
        assertNotNull(attributes);
        assertTrue(attributes.isEmpty());
    }
}

package org.apereo.cas;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalAttributeRepositoryFetcherCascadeTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Attributes")
@ExtendWith(CasTestExtension.class)
class PrincipalAttributeRepositoryFetcherTests {
    @SpringBootTest(classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class,
        properties = {
            "cas.authn.attribute-repository.json[0].location=classpath:/json-attribute-repository.json",
            "cas.authn.attribute-repository.json[0].order=1",
            "cas.authn.attribute-repository.json[0].id=JSON",

            "cas.authn.attribute-repository.groovy[0].location=classpath:/GroovyAttributeRepository.groovy",
            "cas.authn.attribute-repository.groovy[0].order=2",
            "cas.authn.attribute-repository.groovy[0].id=GROOVY",

            "cas.authn.attribute-repository.core.require-all-repository-sources=true"
        })
    static class BaseTests {
        @Autowired
        @Qualifier("aggregatingAttributeRepository")
        protected PersonAttributeDao aggregatingAttributeRepository;
    }

    @Nested
    class DefaultTests extends BaseTests {
        @Test
        void verifyOperation() {
            val attributes = PrincipalAttributeRepositoryFetcher.builder()
                .attributeRepository(aggregatingAttributeRepository)
                .principalId("casuser-whatever")
                .currentPrincipal(CoreAuthenticationTestUtils.getPrincipal("current-cas"))
                .build()
                .fromAllAttributeRepositories()
                .retrieve();
            assertNotNull(attributes);
            assertTrue(attributes.isEmpty());
        }
    }


    @TestPropertySource(properties = "cas.person-directory.active-attribute-repository-ids=")
    @Nested
    class NoActiveRepositoryTests extends BaseTests {
        @Test
        void verifyOperation() {
            val attributes = PrincipalAttributeRepositoryFetcher.builder()
                .attributeRepository(aggregatingAttributeRepository)
                .principalId("friabili")
                .build()
                .retrieve();
            assertNotNull(attributes);
            assertTrue(attributes.isEmpty());
        }
    }

    @TestPropertySource(properties = "cas.person-directory.active-attribute-repository-ids=GROOVY,JSON")
    @Nested
    class SelectiveRepositoryTests extends BaseTests {
        @Test
        void verifyOperation() {
            val attributes = PrincipalAttributeRepositoryFetcher.builder()
                .attributeRepository(aggregatingAttributeRepository)
                .principalId("friabili")
                .activeAttributeRepositoryIdentifiers(Set.of("GROOVY"))
                .build()
                .retrieve();
            assertNotNull(attributes);
            assertTrue(attributes.containsKey("groovyNewName"));
        }
    }

}

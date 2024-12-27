package org.apereo.cas.authentication;

import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrincipalAttributeRepositoryFetcherLdapTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("LdapAttributes")
class PrincipalAttributeRepositoryFetcherLdapTests {

    @TestPropertySource(properties = "cas.authn.attribute-repository.ldap[0].search-filter=(|(cn={username})(name={username}))")
    @Nested
    @EnabledIfListeningOnPort(port = 10389)
    class MultipleFiltersTests extends BasePrincipalAttributeRepositoryFetcherLdapTests {
        @Test
        void verifyOperation() {
            val attributes = PrincipalAttributeRepositoryFetcher.builder()
                .attributeRepository(aggregatingAttributeRepository)
                .principalId(UID)
                .currentPrincipal(CoreAuthenticationTestUtils.getPrincipal("cas"))
                .build()
                .fromAllAttributeRepositories()
                .retrieve();
            assertNotNull(attributes);
            assertFalse(attributes.isEmpty());
        }
    }

    @TestPropertySource(properties = "cas.authn.attribute-repository.ldap[0].search-filter=(|(cn={cn})(title={title}))")
    @Nested
    @EnabledIfListeningOnPort(port = 10389)
    class MultipleFiltersByParameterNameTests extends BasePrincipalAttributeRepositoryFetcherLdapTests {
        @Test
        void verifyOperation() {
            val principal = CoreAuthenticationTestUtils.getPrincipal("cas", Map.of("title", List.of(UID)));
            val attributes = PrincipalAttributeRepositoryFetcher.builder()
                .attributeRepository(aggregatingAttributeRepository)
                .principalId("unknown")
                .currentPrincipal(principal)
                .build()
                .fromAllAttributeRepositories()
                .retrieve();
            assertNotNull(attributes);
            assertFalse(attributes.isEmpty());
        }
    }

    @TestPropertySource(properties = "cas.authn.attribute-repository.ldap[0].search-filter=(|(cn={cn})(title={customParameter}))")
    @Nested
    @EnabledIfListeningOnPort(port = 10389)
    class MultipleFiltersByExtraQueryAttributesTests extends BasePrincipalAttributeRepositoryFetcherLdapTests {
        @Test
        void verifyOperation() {
            val principal = CoreAuthenticationTestUtils.getPrincipal("cas", Map.of());
            val attributes = PrincipalAttributeRepositoryFetcher.builder()
                .attributeRepository(aggregatingAttributeRepository)
                .principalId("unknown")
                .currentPrincipal(principal)
                .queryAttributes(Map.of("customParameter", List.of(UID)))
                .build()
                .fromAllAttributeRepositories()
                .retrieve();
            assertNotNull(attributes);
            assertFalse(attributes.isEmpty());
        }
    }
}

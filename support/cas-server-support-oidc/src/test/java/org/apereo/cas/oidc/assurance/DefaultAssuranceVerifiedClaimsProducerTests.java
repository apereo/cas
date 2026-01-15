package org.apereo.cas.oidc.assurance;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAssuranceVerifiedClaimsProducerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDCAttributes")
class DefaultAssuranceVerifiedClaimsProducerTests {

    @TestPropertySource(properties = "cas.authn.oidc.identity-assurance.verification-source.location=classpath:assurance/id-1.json")
    abstract static class BaseTests extends AbstractOidcTests {
        @Autowired
        @Qualifier(AssuranceVerifiedClaimsProducer.BEAN_NAME)
        protected AssuranceVerifiedClaimsProducer assuranceVerifiedClaimsProducer;
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.discovery.verified-claims-supported=false")
    class NoVerification extends BaseTests {
        @Test
        void verifyOperation() {
            val results = assuranceVerifiedClaimsProducer.produce(new JwtClaims(), "name", "eidas");
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oidc.discovery.verified-claims-supported=true",
        "cas.authn.oidc.discovery.trust-frameworks-supported=id1,id2"
    })
    class NoTrustFramework extends BaseTests {
        @Test
        void verifyOperation() {
            val results = assuranceVerifiedClaimsProducer.produce(new JwtClaims(), "name", "eidas");
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.oidc.discovery.evidence-supported=birthid",
        "cas.authn.oidc.discovery.documents-supported=docs",
        "cas.authn.oidc.discovery.documents-validation-methods-supported=val1",
        "cas.authn.oidc.discovery.documents-verification-methods-supported=ver1",
        "cas.authn.oidc.discovery.electronic-records-supported=license"
    })
    class NoEvidence extends BaseTests {
        @Test
        void verifyOperation() {
            val results = assuranceVerifiedClaimsProducer.produce(new JwtClaims(), "name", "it_spid");
            assertTrue(results.containsKey(DefaultAssuranceVerifiedClaimsProducer.CLAIM_NAME_VERIFIED_CLAIMS));
        }
    }
}

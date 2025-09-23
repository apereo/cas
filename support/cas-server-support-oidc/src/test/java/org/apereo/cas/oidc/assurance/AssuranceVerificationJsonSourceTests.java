package org.apereo.cas.oidc.assurance;

import org.apereo.cas.oidc.AbstractOidcTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AssuranceVerificationJsonSourceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.oidc.identity-assurance.verification-source.location=classpath:assurance/id-1.json")
class AssuranceVerificationJsonSourceTests extends AbstractOidcTests {
    @Autowired
    @Qualifier(AssuranceVerificationSource.BEAN_NAME)
    private AssuranceVerificationSource assuranceVerificationSource;

    @Test
    void verifyOperation() {
        val verifications = assuranceVerificationSource.load();
        assertEquals(8, verifications.size());
        verifications.forEach(v -> assertNotNull(v.toJson()));
    }
}

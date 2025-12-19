package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.CacheCredentialsMetaDataPopulator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CacheCredentialsMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("AuthenticationMetadata")
class CacheCredentialsMetaDataPopulatorTests {

    @Test
    void verifyPasswordAsAuthenticationAttribute() {
        val populator = new CacheCredentialsMetaDataPopulator();
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val builder = DefaultAuthenticationBuilder.newInstance(CoreAuthenticationTestUtils.getAuthentication());
        populator.populateAttributes(builder, CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credential));
        val authn = builder.build();
        assertTrue(authn.containsAttribute(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD));
        assertEquals(credential.toPassword(), authn.getAttributes().get(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD).getFirst().toString());
    }

}

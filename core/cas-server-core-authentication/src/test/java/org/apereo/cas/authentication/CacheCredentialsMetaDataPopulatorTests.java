package org.apereo.cas.authentication;

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
@Tag("Authentication")
public class CacheCredentialsMetaDataPopulatorTests {

    @Test
    public void verifyPasswordAsAuthenticationAttribute() {
        val populator = new CacheCredentialsMetaDataPopulator();

        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val builder = DefaultAuthenticationBuilder.newInstance(CoreAuthenticationTestUtils.getAuthentication());
        populator.populateAttributes(builder, new DefaultAuthenticationTransactionFactory().newTransaction(c));
        val authn = builder.build();
        assertTrue(authn.getAttributes().containsKey(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD));
        assertEquals(c.getPassword(), authn.getAttributes().get(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD).get(0).toString());
    }


}

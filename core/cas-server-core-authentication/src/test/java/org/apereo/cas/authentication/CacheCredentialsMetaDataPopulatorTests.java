package org.apereo.cas.authentication;

import org.apereo.cas.authentication.metadata.CacheCredentialsMetaDataPopulator;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link CacheCredentialsMetaDataPopulator}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class CacheCredentialsMetaDataPopulatorTests {

    @Test
    public void verifyPasswordAsAuthenticationAttribute() {
        final CacheCredentialsMetaDataPopulator populator = new CacheCredentialsMetaDataPopulator();

        final UsernamePasswordCredential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationBuilder builder = DefaultAuthenticationBuilder.newInstance(CoreAuthenticationTestUtils.getAuthentication());
        populator.populateAttributes(builder, AuthenticationTransaction.wrap(c));
        final Authentication authn = builder.build();
        assertTrue(authn.getAttributes().containsKey(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD));
        assertTrue(authn.getAttributes().get(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD)
                .equals(c.getPassword()));
    }


}

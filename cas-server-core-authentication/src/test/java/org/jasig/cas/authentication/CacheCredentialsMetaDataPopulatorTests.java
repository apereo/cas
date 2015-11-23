package org.jasig.cas.authentication;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link org.jasig.cas.authentication.CacheCredentialsMetaDataPopulator}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class CacheCredentialsMetaDataPopulatorTests {

    @Test
    public void verifyPasswordAsAuthenticationAttribute() {
        final CacheCredentialsMetaDataPopulator populator = new CacheCredentialsMetaDataPopulator();

        final UsernamePasswordCredential c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        final AuthenticationBuilder builder = DefaultAuthenticationBuilder.newInstance(TestUtils.getAuthentication());
        populator.populateAttributes(builder, c);
        final Authentication authn = builder.build();
        assertTrue(authn.getAttributes().containsKey(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD));
        assertTrue(authn.getAttributes().get(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD)
                .equals(c.getPassword()));
    }


}

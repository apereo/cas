package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.metadata.CacheCredentialsMetaDataPopulator;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link CacheCredentialsMetaDataPopulator}.
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class CacheCredentialsMetaDataPopulatorTests {

    @Test
    public void verifyPasswordAsAuthenticationAttribute() {
        final var populator = new CacheCredentialsMetaDataPopulator();

        final var c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final var builder = DefaultAuthenticationBuilder.newInstance(CoreAuthenticationTestUtils.getAuthentication());
        populator.populateAttributes(builder, DefaultAuthenticationTransaction.of(c));
        final var authn = builder.build();
        assertTrue(authn.getAttributes().containsKey(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD));
        assertTrue(authn.getAttributes().get(UsernamePasswordCredential.AUTHENTICATION_ATTRIBUTE_PASSWORD)
                .equals(c.getPassword()));
    }


}

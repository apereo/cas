package org.apereo.cas.authentication;

import org.apereo.cas.authentication.metadata.AuthenticationCredentialTypeMetaDataPopulator;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link AuthenticationCredentialTypeMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AuthenticationCredentialTypeMetaDataPopulatorTests {
    private AuthenticationCredentialTypeMetaDataPopulator populator =
        new AuthenticationCredentialTypeMetaDataPopulator();

    @Test
    public void verifyPopulator() {
        final var credentials = new UsernamePasswordCredential();
        final var builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, DefaultAuthenticationTransaction.of(credentials));
        final var auth = builder.build();
        assertEquals(
            credentials.getClass().getSimpleName(),
            auth.getAttributes().get(Credential.CREDENTIAL_TYPE_ATTRIBUTE));
    }
}

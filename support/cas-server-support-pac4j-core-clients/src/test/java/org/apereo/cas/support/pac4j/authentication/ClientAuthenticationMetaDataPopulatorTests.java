package org.apereo.cas.support.pac4j.authentication;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.authentication.principal.ClientCredential;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.credentials.UsernamePasswordCredentials;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ClientAuthenticationMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class ClientAuthenticationMetaDataPopulatorTests {
    @Test
    public void verifySupports() {
        val populator = new ClientAuthenticationMetaDataPopulator();
        val clintCreds = new ClientCredential(
            new UsernamePasswordCredentials("casuser", "pa$$"), "FacebookClient");
        assertTrue(populator.supports(clintCreds));
    }

    @Test
    public void verifyAttribute() {
        val populator = new ClientAuthenticationMetaDataPopulator();
        val credentials = new ClientCredential(
            new UsernamePasswordCredentials("casuser", "pa$$"), "FacebookClient");
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        populator.populateAttributes(builder, DefaultAuthenticationTransaction.of(credentials));
        val auth = builder.build();
        assertNotNull(auth.getAttributes().get(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME));

    }
}

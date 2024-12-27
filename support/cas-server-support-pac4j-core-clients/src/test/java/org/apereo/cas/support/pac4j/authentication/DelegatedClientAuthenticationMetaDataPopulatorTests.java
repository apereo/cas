package org.apereo.cas.support.pac4j.authentication;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ClientCredential;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.profile.CommonProfile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientAuthenticationMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("AuthenticationMetadata")
class DelegatedClientAuthenticationMetaDataPopulatorTests {
    @Test
    void verifySupports() {
        val populator = new DelegatedClientAuthenticationMetaDataPopulator();
        val clientCreds = new ClientCredential(
            new UsernamePasswordCredentials("casuser", "pa$$"), "CasClient");
        assertTrue(populator.supports(clientCreds));
    }

    @Test
    void verifyProfileWithCreds() {
        val populator = new DelegatedClientAuthenticationMetaDataPopulator();
        val clientCreds = new ClientCredential("CasClient", new CommonProfile());
        assertNotNull(clientCreds.getUserProfile());
        assertNotNull(clientCreds.getId());
        assertTrue(populator.supports(clientCreds));
    }

    @Test
    void verifyAttribute() {
        val populator = new DelegatedClientAuthenticationMetaDataPopulator();
        val credentials = new ClientCredential(
            new UsernamePasswordCredentials("casuser", "pa$$"), "CasClient");
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        populator.populateAttributes(builder, CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credentials));
        val auth = builder.build();
        assertNotNull(auth.getAttributes().get(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME));

    }
}

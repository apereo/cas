package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.AuthenticationDateAttributeMetaDataPopulator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuthenticationDateAttributeMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class AuthenticationDateAttributeMetaDataPopulatorTests {
    private final AuthenticationDateAttributeMetaDataPopulator populator =
        new AuthenticationDateAttributeMetaDataPopulator();

    @Test
    public void verifyPopulator() {
        val credentials = new UsernamePasswordCredential();
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, DefaultAuthenticationTransaction.of(credentials));
        val auth = builder.build();
        assertNotNull(auth.getAttributes().get(AuthenticationManager.AUTHENTICATION_DATE_ATTRIBUTE));
    }
}

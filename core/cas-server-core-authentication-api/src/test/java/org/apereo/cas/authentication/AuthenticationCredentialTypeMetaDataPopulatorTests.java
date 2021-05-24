package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.AuthenticationCredentialTypeMetaDataPopulator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuthenticationCredentialTypeMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AuthenticationMetadata")
public class AuthenticationCredentialTypeMetaDataPopulatorTests {
    private final AuthenticationCredentialTypeMetaDataPopulator populator =
        new AuthenticationCredentialTypeMetaDataPopulator();

    @Test
    public void verifyPopulator() {
        val credentials = new UsernamePasswordCredential();
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        this.populator.populateAttributes(builder, new DefaultAuthenticationTransactionFactory().newTransaction(credentials));
        val auth = builder.build();
        assertEquals(
            credentials.getClass().getSimpleName(),
            auth.getAttributes().get(Credential.CREDENTIAL_TYPE_ATTRIBUTE).get(0));
    }

    @Test
    public void verifyPopulatorMultipleTimes() {
        val credentials = new UsernamePasswordCredential();
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        IntStream.rangeClosed(1, 2)
            .forEach(i -> populator.populateAttributes(builder, new DefaultAuthenticationTransactionFactory().newTransaction(credentials)));
        val auth = builder.build();
        val result = auth.getAttributes().get(Credential.CREDENTIAL_TYPE_ATTRIBUTE);
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}

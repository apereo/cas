package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.AuthenticationDateAttributeMetaDataPopulator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationDateAttributeMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("AuthenticationMetadata")
class AuthenticationDateAttributeMetaDataPopulatorTests {
    private final AuthenticationDateAttributeMetaDataPopulator populator =
        new AuthenticationDateAttributeMetaDataPopulator();

    @Test
    void verifyPopulator() {
        val credentials = new UsernamePasswordCredential();
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        populator.populateAttributes(builder, CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credentials));
        val auth = builder.build();
        assertNotNull(auth.getAttributes().get(AuthenticationManager.AUTHENTICATION_DATE_ATTRIBUTE));
        assertFalse(populator.supports(null));
        assertFalse(populator.supports(mock(MultifactorAuthenticationCredential.class)));
    }

    @Test
    void verifyPopulatorMultipleTimes() {
        val credentials = new UsernamePasswordCredential();
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        IntStream.range(1, 5)
            .forEach(i -> populator.populateAttributes(builder, CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credentials)));
        val auth = builder.build();
        val result = auth.getAttributes().get(AuthenticationManager.AUTHENTICATION_DATE_ATTRIBUTE);
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}

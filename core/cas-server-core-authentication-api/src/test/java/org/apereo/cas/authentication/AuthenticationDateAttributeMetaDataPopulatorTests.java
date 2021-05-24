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
public class AuthenticationDateAttributeMetaDataPopulatorTests {
    private final AuthenticationDateAttributeMetaDataPopulator populator =
        new AuthenticationDateAttributeMetaDataPopulator();

    @Test
    public void verifyPopulator() {
        val credentials = new UsernamePasswordCredential();
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        populator.populateAttributes(builder, new DefaultAuthenticationTransactionFactory().newTransaction(credentials));
        val auth = builder.build();
        assertNotNull(auth.getAttributes().get(AuthenticationManager.AUTHENTICATION_DATE_ATTRIBUTE));
        assertFalse(populator.supports(null));
        assertFalse(populator.supports(mock(MultifactorAuthenticationCredential.class)));
    }

    @Test
    public void verifyPopulatorMultipleTimes() {
        val credentials = new UsernamePasswordCredential();
        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        IntStream.range(1, 5)
            .forEach(i -> populator.populateAttributes(builder, new DefaultAuthenticationTransactionFactory().newTransaction(credentials)));
        val auth = builder.build();
        val result = auth.getAttributes().get(AuthenticationManager.AUTHENTICATION_DATE_ATTRIBUTE);
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}

package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateAuthenticationMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AuthenticationMetadata")
class SurrogateAuthenticationMetaDataPopulatorTests {
    @Test
    void verifyAction() throws Throwable {
        val populator = new SurrogateAuthenticationMetaDataPopulator();
        assertFalse(populator.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));

        val credential = new UsernamePasswordCredential();
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("cassurrogate"));
        credential.setUsername("casuser");
        credential.assignPassword("password");

        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        assertThrows(SurrogateAuthenticationException.class,
            () -> populator.populateAttributes(builder, mock(AuthenticationTransaction.class)));
        populator.populateAttributes(builder, CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credential));
        val auth = builder.build();
        assertTrue(auth.containsAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED));
        assertTrue(auth.containsAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL));
        assertTrue(auth.containsAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER));

    }
}

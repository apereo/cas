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
public class SurrogateAuthenticationMetaDataPopulatorTests {
    @Test
    public void verifyAction() {
        val p = new SurrogateAuthenticationMetaDataPopulator();
        assertFalse(p.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));

        val credential = new UsernamePasswordCredential();
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("cassurrogate"));
        credential.setUsername("casuser");
        credential.assignPassword("password");

        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        assertThrows(SurrogateAuthenticationException.class,
            () -> p.populateAttributes(builder, mock(AuthenticationTransaction.class)));
        p.populateAttributes(builder, new DefaultAuthenticationTransactionFactory().newTransaction(credential));
        val auth = builder.build();
        assertTrue(auth.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED));
        assertTrue(auth.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL));
        assertTrue(auth.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER));

    }
}

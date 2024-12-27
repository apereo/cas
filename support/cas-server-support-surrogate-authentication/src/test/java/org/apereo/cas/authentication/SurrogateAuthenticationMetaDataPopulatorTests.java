package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.test.CasTestExtension;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateAuthenticationMetaDataPopulatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AuthenticationMetadata")
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class,
    properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@Tag("Authentication")
@ExtendWith(CasTestExtension.class)
class SurrogateAuthenticationMetaDataPopulatorTests {
    @Autowired
    @Qualifier("surrogateAuthenticationMetadataPopulator")
    private AuthenticationMetaDataPopulator surrogateAuthenticationMetadataPopulator;
    
    @Test
    void verifyAction() {
        assertFalse(surrogateAuthenticationMetadataPopulator.supports(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword()));

        val credential = new UsernamePasswordCredential();
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("cassurrogate"));
        credential.setUsername("casuser");
        credential.assignPassword("password");

        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder();
        assertThrows(SurrogateAuthenticationException.class,
            () -> surrogateAuthenticationMetadataPopulator.populateAttributes(builder, mock(AuthenticationTransaction.class)));
        surrogateAuthenticationMetadataPopulator.populateAttributes(builder,
            CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credential));
        val authentication = builder.build();
        assertTrue(authentication.containsAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED));
        assertTrue(authentication.containsAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL));
        assertTrue(authentication.containsAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER));

    }
}

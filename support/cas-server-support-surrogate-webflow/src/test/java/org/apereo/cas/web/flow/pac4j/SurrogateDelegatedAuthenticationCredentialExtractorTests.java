package org.apereo.cas.web.flow.pac4j;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.config.CasDelegatedAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasSurrogateAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import org.apereo.cas.web.flow.action.BaseSurrogateAuthenticationTests;
import org.apereo.cas.web.flow.passwordless.SurrogatePasswordlessAuthenticationRequestParser;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.credentials.TokenCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateDelegatedAuthenticationCredentialExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Delegation")
@SpringBootTest(classes = {
    CasSurrogateAuthenticationWebflowAutoConfiguration.class,
    CasDelegatedAuthenticationAutoConfiguration.class,
    BaseSurrogateAuthenticationTests.SharedTestConfiguration.class
}, properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@ExtendWith(CasTestExtension.class)
class SurrogateDelegatedAuthenticationCredentialExtractorTests {
    @Autowired
    @Qualifier("surrogateDelegatedPasswordlessAuthenticationCredentialExtractor")
    private DelegatedAuthenticationCredentialExtractor delegatedAuthenticationCredentialExtractor;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyClientCredentialExtracted() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val client = mock(BaseClient.class);

        val uid = UUID.randomUUID().toString();
        val passwordlessRequest = PasswordlessAuthenticationRequest
            .builder()
            .properties(Map.of(SurrogatePasswordlessAuthenticationRequestParser.PROPERTY_SURROGATE_USERNAME, "cassurrogate"))
            .username(uid)
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationRequest(context, passwordlessRequest);

        val tokenCredentials = new TokenCredentials(uid);
        when(client.getCredentials(any())).thenReturn(Optional.of(tokenCredentials));
        when(client.validateCredentials(any(), any())).thenReturn(Optional.of(tokenCredentials));

        val credentials = delegatedAuthenticationCredentialExtractor.extract(client, context).get();
        assertNotNull(credentials);
        val trait = credentials.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class);
        assertTrue(trait.isPresent());
        assertEquals("cassurrogate", trait.get().getSurrogateUsername());
        when(client.getCredentials(any())).thenReturn(Optional.empty());
        assertTrue(delegatedAuthenticationCredentialExtractor.extract(client, context).isEmpty());
    }
}

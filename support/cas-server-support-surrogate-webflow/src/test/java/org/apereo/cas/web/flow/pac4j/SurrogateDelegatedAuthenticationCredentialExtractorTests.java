package org.apereo.cas.web.flow.pac4j;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.config.Pac4jAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.Pac4jDelegatedAuthenticationConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationDelegationConfiguration;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import org.apereo.cas.web.flow.action.BaseSurrogateAuthenticationTests;
import org.apereo.cas.web.flow.passwordless.SurrogatePasswordlessAuthenticationRequestParser;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.credentials.TokenCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

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
    SurrogateAuthenticationDelegationConfiguration.class,
    Pac4jDelegatedAuthenticationConfiguration.class,
    Pac4jAuthenticationEventExecutionPlanConfiguration.class,
    BaseSurrogateAuthenticationTests.SharedTestConfiguration.class
},
    properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
public class SurrogateDelegatedAuthenticationCredentialExtractorTests {
    @Autowired
    @Qualifier("delegatedAuthenticationCredentialExtractor")
    private DelegatedAuthenticationCredentialExtractor delegatedAuthenticationCredentialExtractor;

    @Test
    public void verifyClientCredentialExtracted() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val client = mock(BaseClient.class);

        val uid = UUID.randomUUID().toString();
        val passwordlessRequest = PasswordlessAuthenticationRequest.builder()
            .properties(Map.of(SurrogatePasswordlessAuthenticationRequestParser.PROPORTY_SURROGATE_USERNAME, "cassurrogate"))
            .username(uid).build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationRequest(context, passwordlessRequest);
        
        when(client.getCredentials(any(), any(), any())).thenReturn(Optional.of(new TokenCredentials(uid)));
        val cc = delegatedAuthenticationCredentialExtractor.extract(client, context);
        assertNotNull(cc);
        assertTrue(cc.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class).isPresent());
        
        when(client.getCredentials(any(), any(), any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> delegatedAuthenticationCredentialExtractor.extract(client, context));
    }
}

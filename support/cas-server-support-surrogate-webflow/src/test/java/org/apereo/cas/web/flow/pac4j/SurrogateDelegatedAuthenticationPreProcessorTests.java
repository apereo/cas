package org.apereo.cas.web.flow.pac4j;

import org.apereo.cas.authentication.SurrogatePrincipal;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.config.Pac4jAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.Pac4jDelegatedAuthenticationConfiguration;
import org.apereo.cas.config.SurrogateAuthenticationDelegationConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.action.BaseSurrogateAuthenticationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.client.BaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateDelegatedAuthenticationPreProcessorTests}.
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
public class SurrogateDelegatedAuthenticationPreProcessorTests {
    @Autowired
    @Qualifier("surrogateDelegatedAuthenticationPreProcessor")
    private DelegatedAuthenticationPreProcessor surrogateDelegatedAuthenticationPreProcessor;

    @Test
    public void verifySurrogatePrincipalFound() {
        val credential = new BasicIdentifiableCredential("casuser");
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("cassurrogate"));
        val surrogatePrincipal = surrogateDelegatedAuthenticationPreProcessor.process(RegisteredServiceTestUtils.getPrincipal("casuser"),
            mock(BaseClient.class), credential, RegisteredServiceTestUtils.getService());
        assertTrue(surrogatePrincipal instanceof SurrogatePrincipal);
    }

    @Test
    public void verifyDefault() {
        val credential = new BasicIdentifiableCredential("casuser");
        val surrogatePrincipal = surrogateDelegatedAuthenticationPreProcessor.process(RegisteredServiceTestUtils.getPrincipal("casuser"),
            mock(BaseClient.class), credential, RegisteredServiceTestUtils.getService());
        assertFalse(surrogatePrincipal instanceof SurrogatePrincipal);
    }
}

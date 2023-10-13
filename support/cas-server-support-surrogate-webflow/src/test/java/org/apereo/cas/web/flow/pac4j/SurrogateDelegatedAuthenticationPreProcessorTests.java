package org.apereo.cas.web.flow.pac4j;

import org.apereo.cas.authentication.SurrogatePrincipal;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.config.DelegatedAuthenticationConfiguration;
import org.apereo.cas.config.DelegatedAuthenticationEventExecutionPlanConfiguration;
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
    DelegatedAuthenticationConfiguration.class,
    DelegatedAuthenticationEventExecutionPlanConfiguration.class,
    BaseSurrogateAuthenticationTests.SharedTestConfiguration.class
},
    properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
class SurrogateDelegatedAuthenticationPreProcessorTests {
    @Autowired
    @Qualifier("surrogateDelegatedAuthenticationPreProcessor")
    private DelegatedAuthenticationPreProcessor surrogateDelegatedAuthenticationPreProcessor;

    @Test
    void verifySurrogatePrincipalFound() throws Throwable {
        val credential = new BasicIdentifiableCredential("casuser");
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("cassurrogate"));
        val surrogatePrincipal = surrogateDelegatedAuthenticationPreProcessor.process(RegisteredServiceTestUtils.getPrincipal("casuser"),
            mock(BaseClient.class), credential, RegisteredServiceTestUtils.getService());
        assertInstanceOf(SurrogatePrincipal.class, surrogatePrincipal);
    }

    @Test
    void verifyDefault() throws Throwable {
        val credential = new BasicIdentifiableCredential("casuser");
        val surrogatePrincipal = surrogateDelegatedAuthenticationPreProcessor.process(RegisteredServiceTestUtils.getPrincipal("casuser"),
            mock(BaseClient.class), credential, RegisteredServiceTestUtils.getService());
        assertFalse(surrogatePrincipal instanceof SurrogatePrincipal);
    }
}

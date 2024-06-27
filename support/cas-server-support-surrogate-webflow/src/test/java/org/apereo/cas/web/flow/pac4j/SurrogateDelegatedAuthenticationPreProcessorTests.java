package org.apereo.cas.web.flow.pac4j;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.SurrogatePrincipal;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.config.CasDelegatedAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasSurrogateAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.action.BaseSurrogateAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.client.BaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Map;
import java.util.UUID;
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
    CasSurrogateAuthenticationWebflowAutoConfiguration.class,
    CasDelegatedAuthenticationAutoConfiguration.class,
    BaseSurrogateAuthenticationTests.SharedTestConfiguration.class
}, properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@ExtendWith(CasTestExtension.class)
class SurrogateDelegatedAuthenticationPreProcessorTests {
    @Autowired
    @Qualifier("surrogateDelegatedAuthenticationPreProcessor")
    private DelegatedAuthenticationPreProcessor surrogateDelegatedAuthenticationPreProcessor;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifySurrogatePrincipalFound() throws Throwable {
        val service = CoreAuthenticationTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
        servicesManager.save(registeredService);


        val credential = new BasicIdentifiableCredential("casuser");
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("cassurrogate"));
        val surrogatePrincipal = surrogateDelegatedAuthenticationPreProcessor.process(RegisteredServiceTestUtils.getPrincipal("casuser"),
            mock(BaseClient.class), credential, service);
        assertInstanceOf(SurrogatePrincipal.class, surrogatePrincipal);
    }

    @Test
    void verifyDefault() throws Throwable {
        val service = CoreAuthenticationTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
        servicesManager.save(registeredService);

        val credential = new BasicIdentifiableCredential("casuser");
        val surrogatePrincipal = surrogateDelegatedAuthenticationPreProcessor.process(RegisteredServiceTestUtils.getPrincipal("casuser"),
            mock(BaseClient.class), credential, service);
        assertFalse(surrogatePrincipal instanceof SurrogatePrincipal);
    }
}

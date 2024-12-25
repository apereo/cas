package org.apereo.cas.ticket;

import org.apereo.cas.authentication.AuthenticationResultBuilderFactory;
import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceSurrogatePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.Ordered;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateServiceTicketGeneratorAuthorityTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("Impersonation")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.attribute-repository.stub.attributes.uid=uid",
        "cas.authn.attribute-repository.stub.attributes.eduPersonAffiliation=developer",
        "cas.authn.surrogate.simple.surrogates.casuser=surrogate"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
class SurrogateServiceTicketGeneratorAuthorityTests {
    @Autowired
    @Qualifier("surrogateServiceTicketGeneratorAuthority")
    private ServiceTicketGeneratorAuthority surrogateServiceTicketGeneratorAuthority;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("authenticationResultBuilderFactory")
    private AuthenticationResultBuilderFactory authenticationResultBuilderFactory;

    @Test
    void verifyOperation() throws Throwable {
        assertEquals(Ordered.HIGHEST_PRECEDENCE, surrogateServiceTicketGeneratorAuthority.getOrder());

        val credential = new UsernamePasswordCredential();
        credential.getCredentialMetadata()
            .addTrait(new SurrogateCredentialTrait("surrogate"));
        credential.setUsername("casuser");
        val authenticationResult = authenticationResultBuilderFactory.newBuilder()
            .collect(RegisteredServiceTestUtils.getAuthentication(credential))
            .build();

        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
        servicesManager.save(registeredService);

        registeredService.setSurrogatePolicy(new DefaultRegisteredServiceSurrogatePolicy().setEnabled(false));
        assertTrue(surrogateServiceTicketGeneratorAuthority.supports(authenticationResult, service));
        assertThrows(SurrogateAuthenticationException.class,
            () -> surrogateServiceTicketGeneratorAuthority.shouldGenerate(authenticationResult, service));

        registeredService.setSurrogatePolicy(new DefaultRegisteredServiceSurrogatePolicy().setEnabled(true));
        servicesManager.save(registeredService);
        assertTrue(surrogateServiceTicketGeneratorAuthority.shouldGenerate(authenticationResult, service));
    }
}

package org.apereo.cas.authentication.surrogate;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceSurrogatePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingSurrogateAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Tag("Impersonation")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.surrogate.simple.surrogates.casuser=user1,user2,user3",
        "cas.authn.surrogate.json.location=classpath:/surrogates.json"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ChainingSurrogateAuthenticationServiceTests {
    @Autowired
    @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
    private SurrogateAuthenticationService surrogateService;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyOperation() throws Throwable {
        assertFalse(surrogateService.isWildcardedAccount(List.of("cassystem"), Optional.empty()));
        assertFalse(surrogateService.isWildcardedAccount("cassystem", RegisteredServiceTestUtils.getPrincipal(), Optional.empty()));

        val accounts = surrogateService.getImpersonationAccounts("casuser", Optional.empty());
        assertTrue(accounts.contains("user1"));
        assertTrue(accounts.contains("user2"));
        assertTrue(accounts.contains("user3"));
        assertTrue(surrogateService.canImpersonate("user3", RegisteredServiceTestUtils.getPrincipal("casuser"), Optional.empty()));
        assertTrue(surrogateService.canImpersonate("tomhanks", RegisteredServiceTestUtils.getPrincipal("adminuser"), Optional.empty()));
    }
    
    @Test
    void verifyOperationDisabledByService() throws Throwable {
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.setSurrogatePolicy(new DefaultRegisteredServiceSurrogatePolicy().setEnabled(false));
        servicesManager.save(registeredService);

        assertFalse(surrogateService.isWildcardedAccount(List.of("cassystem"), Optional.of(service)));
        assertFalse(surrogateService.isWildcardedAccount("cassystem", RegisteredServiceTestUtils.getPrincipal(), Optional.of(service)));

        val accounts = surrogateService.getImpersonationAccounts("casuser", Optional.of(service));
        assertTrue(accounts.isEmpty());
        assertFalse(surrogateService.canImpersonate("user3", RegisteredServiceTestUtils.getPrincipal("casuser"), Optional.of(service)));
        assertFalse(surrogateService.canImpersonate("tomhanks", RegisteredServiceTestUtils.getPrincipal("adminuser"), Optional.of(service)));
    }
}

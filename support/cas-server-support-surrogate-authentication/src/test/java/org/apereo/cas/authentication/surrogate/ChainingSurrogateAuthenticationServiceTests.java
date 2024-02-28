package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingSurrogateAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Tag("Impersonation")
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.surrogate.simple.surrogates.casuser=user1,user2,user3",
        "cas.authn.surrogate.json.location=classpath:/surrogates.json"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ChainingSurrogateAuthenticationServiceTests {
    @Autowired
    @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
    private SurrogateAuthenticationService service;

    @Test
    void verifyOperation() throws Throwable {
        assertFalse(service.isWildcardedAccount(List.of("cassystem")));
        assertFalse(service.isWildcardedAccount("cassystem", RegisteredServiceTestUtils.getPrincipal()));

        val accounts = service.getImpersonationAccounts("casuser");
        assertTrue(accounts.contains("user1"));
        assertTrue(accounts.contains("user2"));
        assertTrue(accounts.contains("user3"));
        assertTrue(service.canImpersonate("user3", RegisteredServiceTestUtils.getPrincipal("casuser"), Optional.empty()));
        assertTrue(service.canImpersonate("tomhanks", RegisteredServiceTestUtils.getPrincipal("adminuser"), Optional.empty()));
    }
}

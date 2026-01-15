package org.apereo.cas.mfa.simple.ticket;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("ExpirationPolicy")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilderTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyTicketType() {
        val builder = new CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilder(casProperties);
       assertNotNull(builder.toTicketExpirationPolicy());
    }
}

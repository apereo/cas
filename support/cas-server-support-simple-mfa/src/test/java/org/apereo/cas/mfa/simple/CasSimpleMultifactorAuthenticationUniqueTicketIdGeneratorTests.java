package org.apereo.cas.mfa.simple;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
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
 * This is {@link CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
class CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests {

    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationUniqueTicketIdGenerator")
    private UniqueTicketIdGenerator casSimpleMultifactorAuthenticationUniqueTicketIdGenerator;

    @Test
    void verifyOperation() throws Throwable {
        val token = casSimpleMultifactorAuthenticationUniqueTicketIdGenerator.getNewTicketId("CAS");
        assertNotNull(token);
    }
}

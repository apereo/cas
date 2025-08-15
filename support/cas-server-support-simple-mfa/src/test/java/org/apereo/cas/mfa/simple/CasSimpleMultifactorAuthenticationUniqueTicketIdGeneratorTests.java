package org.apereo.cas.mfa.simple;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
class CasSimpleMultifactorAuthenticationUniqueTicketIdGeneratorTests {

    @SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    abstract static class BaseTests {
        @Autowired
        @Qualifier("casSimpleMultifactorAuthenticationUniqueTicketIdGenerator")
        protected UniqueTicketIdGenerator casSimpleMultifactorAuthenticationUniqueTicketIdGenerator;
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.simple.token.core.token-type=NUMERIC")
    public class NumericCodes extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val token = casSimpleMultifactorAuthenticationUniqueTicketIdGenerator.getNewTicketId("CAS");
            assertTrue(NumberUtils.isDigits(token.replace("CAS-", StringUtils.EMPTY)));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.simple.token.core.token-type=ALPHANUMERIC")
    public class AlphanumericCodes extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val token = casSimpleMultifactorAuthenticationUniqueTicketIdGenerator.getNewTicketId("CAS");
            assertTrue(StringUtils.isAlphanumeric(token.replace("CAS-", StringUtils.EMPTY)));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.simple.token.core.token-type=ALPHABETIC")
    public class AlphabeticCodes extends BaseTests {
        @Test
        void verifyOperation() throws Throwable {
            val token = casSimpleMultifactorAuthenticationUniqueTicketIdGenerator.getNewTicketId("CAS");
            assertTrue(StringUtils.isAlpha(token.replace("CAS-", StringUtils.EMPTY)));
        }
    }
}

package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.JpaYubiKeyConfiguration;
import org.apereo.cas.config.YubiKeyConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.authentication.YubiKeyAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.support.authentication.YubiKeyAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    YubiKeyConfiguration.class,
    YubiKeyAuthenticationMultifactorProviderBypassConfiguration.class,
    JpaYubiKeyConfiguration.class,
    JpaYubiKeyAccountRegistryTests.JpaYubiKeyAccountRegistryTestConfiguration.class,
    YubiKeyAuthenticationEventExecutionPlanConfiguration.class,
    CasHibernateJpaConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreHttpConfiguration.class,
    AopAutoConfiguration.class,
    CasThemesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    RefreshAutoConfiguration.class
}, properties = {
    "cas.authn.mfa.yubikey.clientId=18423",
    "cas.authn.mfa.yubikey.secretKey=zAIqhjui12mK8x82oe9qzBEb0As=",
    "cas.jdbc.showSql=true",
    "cas.authn.mfa.yubikey.jpa.ddlAuto=create-drop"
})
@Tag("JDBC")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class JpaYubiKeyAccountRegistryTests {
    private static final String BAD_TOKEN = "123456";

    @Autowired
    @Qualifier("yubiKeyAccountRegistry")
    private YubiKeyAccountRegistry yubiKeyAccountRegistry;
    
    @Test
    public void verifyAccountNotRegistered() {
        assertFalse(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("missing-user"));
    }

    @Test
    public void verifyAccountNotRegisteredWithBadToken() {
        assertFalse(yubiKeyAccountRegistry.registerAccountFor("casuser", BAD_TOKEN));
        assertFalse(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("casuser"));
    }

    @Test
    public void verifyAccountRegistered() {
        assertTrue(yubiKeyAccountRegistry.registerAccountFor("casuser", "cccccccvlidchlffblbghhckbctgethcrtdrruchvlud"));
        assertTrue(yubiKeyAccountRegistry.registerAccountFor("casuser", "cccccccvlidchlffblbghhckbctgethcrtdrruchvluq"));
        assertTrue(yubiKeyAccountRegistry.isYubiKeyRegisteredFor("casuser"));
        assertEquals(1, yubiKeyAccountRegistry.getAccounts().size());
        val account = yubiKeyAccountRegistry.getAccount("casuser");
        account.ifPresent(acct -> assertEquals(2, acct.getDeviceIdentifiers().size()));
    }

    @TestConfiguration("JpaYubiKeyAccountRegistryTestConfiguration")
    @Lazy(false)
    public static class JpaYubiKeyAccountRegistryTestConfiguration {
        @Bean
        @RefreshScope
        public YubiKeyAccountValidator yubiKeyAccountValidator() {
            return (uid, token) -> !token.equals(BAD_TOKEN);
        }
    }
}

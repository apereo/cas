package org.apereo.cas.gauth.token;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.GoogleAuthenticatorJpaConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.authentication.GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.support.authentication.GoogleAuthenticatorAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.gauth.credential.JpaGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link GoogleAuthenticatorJpaTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    JpaGoogleAuthenticatorTokenCredentialRepositoryTests.JpaTestConfiguration.class,
    GoogleAuthenticatorJpaConfiguration.class,
    GoogleAuthenticatorAuthenticationMultifactorProviderBypassConfiguration.class,
    CasHibernateJpaConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreWebflowConfiguration.class,
    GoogleAuthenticatorAuthenticationEventExecutionPlanConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreWebConfiguration.class
}, properties = "cas.jdbc.showSql=true"
)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
@Getter
@Tag("JDBC")
public class GoogleAuthenticatorJpaTokenRepositoryTests extends BaseOneTimeTokenRepositoryTests {
}

package org.apereo.cas.pm.jdbc;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.pm.JdbcPasswordHistoryManagementConfiguration;
import org.apereo.cas.config.pm.JdbcPasswordManagementConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * This is {@link BaseJdbcPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreConfiguration.class,
    CasHibernateJpaConfiguration.class,
    JdbcPasswordManagementConfiguration.class,
    JdbcPasswordHistoryManagementConfiguration.class,
    PasswordManagementConfiguration.class
}, properties = {
    "cas.jdbc.showSql=true",
    "cas.authn.pm.enabled=true",
    "cas.authn.pm.history.enabled=true",
    "cas.authn.pm.jdbc.autoCommit=false",
    "cas.authn.pm.jdbc.sqlSecurityQuestions=SELECT question, answer FROM pm_table_questions WHERE userid=?",
    "cas.authn.pm.jdbc.sqlFindEmail=SELECT email FROM pm_table_accounts WHERE userid=?",
    "cas.authn.pm.jdbc.sqlFindPhone=SELECT phone FROM pm_table_accounts WHERE userid=?",
    "cas.authn.pm.jdbc.sqlChangePassword=UPDATE pm_table_accounts SET password=? WHERE userid=?"
})
@Tag("JDBC")
@EnableTransactionManagement(proxyTargetClass = true)
public abstract class BaseJdbcPasswordManagementServiceTests {
    @Autowired
    @Qualifier("passwordChangeService")
    protected PasswordManagementService passwordChangeService;

    @Autowired
    @Qualifier("jdbcPasswordManagementDataSource")
    protected DataSource jdbcPasswordManagementDataSource;

    @Autowired
    @Qualifier("passwordHistoryService")
    protected PasswordHistoryService passwordHistoryService;

    @Autowired
    @Qualifier("jdbcPasswordManagementTransactionTemplate")
    protected TransactionTemplate jdbcPasswordManagementTransactionTemplate;

}

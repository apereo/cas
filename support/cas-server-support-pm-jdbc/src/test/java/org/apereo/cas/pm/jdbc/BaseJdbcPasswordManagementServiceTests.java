package org.apereo.cas.pm.jdbc;

import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJdbcPasswordManagementAutoConfiguration;
import org.apereo.cas.config.CasPasswordManagementAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionOperations;
import javax.sql.DataSource;

/**
 * This is {@link BaseJdbcPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasJdbcPasswordManagementAutoConfiguration.class,
    CasPasswordManagementAutoConfiguration.class
}, properties = {
    "cas.jdbc.show-sql=false",
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.history.core.enabled=true",
    "cas.authn.pm.jdbc.auto-commit=false",

    "cas.authn.pm.jdbc.sql-get-security-questions=SELECT question, answer FROM pm_table_questions WHERE userid=?",
    "cas.authn.pm.jdbc.sql-delete-security-questions=DELETE FROM pm_table_questions WHERE userid=?",
    "cas.authn.pm.jdbc.sql-update-security-questions=INSERT INTO pm_table_questions(userid, question, answer) VALUES (?,?,?);",
    "cas.authn.pm.jdbc.sql-find-email=SELECT email FROM pm_table_accounts WHERE userid=?",
    "cas.authn.pm.jdbc.sql-find-user=SELECT userid FROM pm_table_accounts WHERE email=?",
    "cas.authn.pm.jdbc.sql-find-phone=SELECT phone FROM pm_table_accounts WHERE userid=?",
    "cas.authn.pm.jdbc.sql-unlock-account=UPDATE pm_table_accounts SET enabled=? WHERE userid=?",
    "cas.authn.pm.jdbc.sql-change-password=UPDATE pm_table_accounts SET password=? WHERE userid=?"
})
@EnableTransactionManagement(proxyTargetClass = false)
@ExtendWith(CasTestExtension.class)
public abstract class BaseJdbcPasswordManagementServiceTests {
    @Autowired
    @Qualifier(PasswordManagementService.DEFAULT_BEAN_NAME)
    protected PasswordManagementService passwordChangeService;

    @Autowired
    @Qualifier("jdbcPasswordManagementDataSource")
    protected DataSource jdbcPasswordManagementDataSource;

    @Autowired
    @Qualifier(PasswordHistoryService.BEAN_NAME)
    protected PasswordHistoryService passwordHistoryService;

    @Autowired
    @Qualifier("jdbcPasswordManagementTransactionTemplate")
    protected TransactionOperations jdbcPasswordManagementTransactionTemplate;

}

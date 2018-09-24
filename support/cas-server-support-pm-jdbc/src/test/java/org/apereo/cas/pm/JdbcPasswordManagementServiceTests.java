package org.apereo.cas.pm;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.JdbcPasswordManagementConfiguration;
import org.apereo.cas.config.PasswordManagementConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

/**
 * This is {@link JdbcPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    JdbcPasswordManagementConfiguration.class,
    PasswordManagementConfiguration.class})
@TestPropertySource(properties = {
    "cas.authn.pm.enabled=true",
    "cas.authn.pm.jdbc.autoCommit=true",
    "cas.authn.pm.jdbc.sqlSecurityQuestions=SELECT question, answer FROM pm_table_questions WHERE userid=?",
    "cas.authn.pm.jdbc.sqlFindEmail=SELECT email FROM pm_table_accounts WHERE userid=?",
    "cas.authn.pm.jdbc.sqlChangePassword=UPDATE pm_table_accounts SET password=? WHERE userid=?"
    })
public class JdbcPasswordManagementServiceTests extends AbstractPasswordManagementTests {
    @Autowired
    @Qualifier("jdbcPasswordManagementDataSource")
    private DataSource jdbcPasswordManagementDataSource;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void before() {
        jdbcTemplate = new JdbcTemplate(this.jdbcPasswordManagementDataSource);

        jdbcTemplate.execute("drop table pm_table_accounts if exists;");
        jdbcTemplate.execute("create table pm_table_accounts (id int, userid varchar(255),"
            + "password varchar(255), email varchar(255));");
        jdbcTemplate.execute("insert into pm_table_accounts values (100, 'casuser', 'password', 'casuser@example.org');");

        jdbcTemplate.execute("drop table pm_table_questions if exists;");
        jdbcTemplate.execute("create table pm_table_questions (id int, userid varchar(255),"
            + " question varchar(255), answer varchar(255));");
        jdbcTemplate.execute("insert into pm_table_questions values (100, 'casuser', 'question1', 'answer1');");
        jdbcTemplate.execute("insert into pm_table_questions values (200, 'casuser', 'question2', 'answer2');");

    }

    @After
    public void after() {
        jdbcTemplate = new JdbcTemplate(this.jdbcPasswordManagementDataSource);

        jdbcTemplate.execute("drop table pm_table_accounts if exists;");
    }
}

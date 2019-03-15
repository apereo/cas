package org.apereo.cas.pm.jdbc;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
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
import org.apereo.cas.config.pm.JdbcPasswordManagementConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

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
public class JdbcPasswordManagementServiceTests {
    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordChangeService;

    @Autowired
    @Qualifier("jdbcPasswordManagementDataSource")
    private DataSource jdbcPasswordManagementDataSource;

    @BeforeEach
    public void before() {
        val jdbcTemplate = new JdbcTemplate(this.jdbcPasswordManagementDataSource);

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

    @Test
    public void verifyUserEmailCanBeFound() {
        val email = passwordChangeService.findEmail("casuser");
        assertEquals("casuser@example.org", email);
    }

    @Test
    public void verifyNullReturnedIfUserEmailCannotBeFound() {
        val email = passwordChangeService.findEmail("unknown");
        assertNull(email);
    }

    @Test
    public void verifyUserQuestionsCanBeFound() {
        val questions = passwordChangeService.getSecurityQuestions("casuser");
        assertEquals(2, questions.size());
        assertTrue(questions.containsKey("question1"));
        assertTrue(questions.containsKey("question2"));
    }

    @Test
    public void verifyUserPasswordChange() {
        val c = new UsernamePasswordCredential("casuser", "password");
        val bean = new PasswordChangeBean();
        bean.setConfirmedPassword("newPassword1");
        bean.setPassword("newPassword1");
        val res = passwordChangeService.change(c, bean);
        assertTrue(res);
    }


}

package org.apereo.cas.pm.jdbc;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link JdbcPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
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
@TestPropertySource(locations = {"classpath:/pm.properties"})
public class JdbcPasswordManagementServiceTests {
    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordChangeService;

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

    @Test
    public void verifyUserEmailCanBeFound() {
        final String email = passwordChangeService.findEmail("casuser");
        assertEquals("casuser@example.org", email);
    }

    @Test
    public void verifyNullReturnedIfUserEmailCannotBeFound() {
        final String email = passwordChangeService.findEmail("unknown");
        assertNull(email);
    }

    @Test
    public void verifyUserQuestionsCanBeFound() {
        final Map questions = passwordChangeService.getSecurityQuestions("casuser");
        assertEquals(2, questions.size());
        assertTrue(questions.containsKey("question1"));
        assertTrue(questions.containsKey("question2"));
    }

    @Test
    public void verifyUserPasswordChange() {
        final Credential c = new UsernamePasswordCredential("casuser", "password");
        final PasswordChangeBean bean = new PasswordChangeBean();
        bean.setConfirmedPassword("newPassword1");
        bean.setPassword("newPassword1");
        final boolean res = passwordChangeService.change(c, bean);
        assertTrue(res);
    }


}

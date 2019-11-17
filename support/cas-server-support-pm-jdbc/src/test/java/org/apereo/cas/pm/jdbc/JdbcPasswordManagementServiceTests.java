package org.apereo.cas.pm.jdbc;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.pm.PasswordChangeRequest;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("JDBC")
public class JdbcPasswordManagementServiceTests extends BaseJdbcPasswordManagementServiceTests {

    @Test
    public void verifyUserEmailCanBeFound() {
        val email = passwordChangeService.findEmail("casuser");
        assertEquals("casuser@example.org", email);
    }

    @Test
    public void verifyPhoneNumberCanBeFound() {
        val phone = passwordChangeService.findPhone("casuser");
        assertEquals("1234567890", phone);
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
        val bean = new PasswordChangeRequest();
        bean.setConfirmedPassword("newPassword1");
        bean.setUsername(c.getUsername());
        bean.setPassword("newPassword1");
        val res = passwordChangeService.change(c, bean);
        assertTrue(res);
    }


    @BeforeEach
    public void before() {
        this.jdbcPasswordManagementTransactionTemplate.executeWithoutResult(action -> {
            val jdbcTemplate = new JdbcTemplate(this.jdbcPasswordManagementDataSource);
            dropTablesBeforeTest(jdbcTemplate);

            jdbcTemplate.execute("create table pm_table_accounts (id int, userid varchar(255),"
                + "password varchar(255), email varchar(255), phone varchar(255));");
            jdbcTemplate.execute("insert into pm_table_accounts values (100, 'casuser', 'password', 'casuser@example.org', '1234567890');");

            jdbcTemplate.execute("create table pm_table_questions (id int, userid varchar(255),"
                + " question varchar(255), answer varchar(255));");
            jdbcTemplate.execute("insert into pm_table_questions values (100, 'casuser', 'question1', 'answer1');");
            jdbcTemplate.execute("insert into pm_table_questions values (200, 'casuser', 'question2', 'answer2');");
        });
    }

    @Transactional
    protected void dropTablesBeforeTest(final JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("drop table pm_table_accounts if exists;");
        jdbcTemplate.execute("drop table pm_table_questions if exists;");
    }
}

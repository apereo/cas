package org.apereo.cas.pm.jdbc;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementQuery;

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
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals("casuser@example.org", email);
        assertNull(passwordChangeService.findEmail(PasswordManagementQuery.builder().username("unknown").build()));
        assertNull(passwordChangeService.findEmail(PasswordManagementQuery.builder().username("baduser").build()));
    }

    @Test
    public void verifyUserCanBeFound() {
        val user = passwordChangeService.findUsername(PasswordManagementQuery.builder().email("casuser@example.org").build());
        assertEquals("casuser", user);
        assertNull(passwordChangeService.findUsername(PasswordManagementQuery.builder().email("unknown").build()));
    }

    @Test
    public void verifyPhoneNumberCanBeFound() {
        val phone = passwordChangeService.findPhone(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals("1234567890", phone);
        assertNull(passwordChangeService.findPhone(PasswordManagementQuery.builder().username("whatever").build()));
        assertNull(passwordChangeService.findPhone(PasswordManagementQuery.builder().username("baduser").build()));
    }


    @Test
    public void verifyUserQuestionsCanBeFound() {
        val questions = passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build());
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
        assertTrue(passwordChangeService.change(c, bean));
        assertFalse(passwordHistoryService.fetch(c.getUsername()).isEmpty());
        assertFalse(passwordChangeService.change(c, bean));
    }

    @Test
    public void verifySecurityQuestions() {
        val query = PasswordManagementQuery.builder().username("casuser").build();
        query.securityQuestion("Q1", "A1");
        passwordChangeService.updateSecurityQuestions(query);
        assertFalse(passwordChangeService.getSecurityQuestions(query).isEmpty());
    }
    
    @BeforeEach
    public void before() {
        this.jdbcPasswordManagementTransactionTemplate.executeWithoutResult(action -> {
            val jdbcTemplate = new JdbcTemplate(this.jdbcPasswordManagementDataSource);
            dropTablesBeforeTest(jdbcTemplate);

            jdbcTemplate.execute("create table pm_table_accounts (userid varchar(255),"
                + "password varchar(255), email varchar(255), phone varchar(255));");
            jdbcTemplate.execute("insert into pm_table_accounts values ('casuser', 'password', 'casuser@example.org', '1234567890');");
            jdbcTemplate.execute("insert into pm_table_accounts values ('baduser', 'password', '', '');");

            jdbcTemplate.execute("create table pm_table_questions (userid varchar(255),"
                + " question varchar(255), answer varchar(255));");
            jdbcTemplate.execute("insert into pm_table_questions values ('casuser', 'question1', 'answer1');");
            jdbcTemplate.execute("insert into pm_table_questions values ('casuser', 'question2', 'answer2');");
        });
    }

    @Transactional
    protected void dropTablesBeforeTest(final JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("drop table pm_table_accounts if exists;");
        jdbcTemplate.execute("drop table pm_table_questions if exists;");
    }
}

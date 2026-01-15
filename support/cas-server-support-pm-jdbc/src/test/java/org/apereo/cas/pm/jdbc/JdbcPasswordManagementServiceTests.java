package org.apereo.cas.pm.jdbc;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
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
class JdbcPasswordManagementServiceTests extends BaseJdbcPasswordManagementServiceTests {

    @Test
    void verifyUserEmailCanBeFound() throws Throwable {
        val email = passwordChangeService.findEmail(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals("casuser@example.org", email);
        assertNull(passwordChangeService.findEmail(PasswordManagementQuery.builder().username("unknown").build()));
        assertNull(passwordChangeService.findEmail(PasswordManagementQuery.builder().username("baduser").build()));
    }

    @Test
    void verifyUserCanBeFound() throws Throwable {
        val user = passwordChangeService.findUsername(PasswordManagementQuery.builder().email("casuser@example.org").build());
        assertEquals("casuser", user);
        assertNull(passwordChangeService.findUsername(PasswordManagementQuery.builder().email("unknown").build()));
    }

    @Test
    void verifyPhoneNumberCanBeFound() throws Throwable {
        val phone = passwordChangeService.findPhone(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals("1234567890", phone);
        assertNull(passwordChangeService.findPhone(PasswordManagementQuery.builder().username("whatever").build()));
        assertNull(passwordChangeService.findPhone(PasswordManagementQuery.builder().username("baduser").build()));
    }


    @Test
    void verifyUserQuestionsCanBeFound() throws Throwable {
        val questions = passwordChangeService.getSecurityQuestions(PasswordManagementQuery.builder().username("casuser").build());
        assertEquals(2, questions.size());
        assertTrue(questions.containsKey("question1"));
        assertTrue(questions.containsKey("question2"));
    }

    @Test
    void verifyUserPasswordChange() throws Throwable {
        val c = new UsernamePasswordCredential("casuser", "password");
        val bean = new PasswordChangeRequest();
        bean.setConfirmedPassword("newPassword1".toCharArray());
        bean.setUsername(c.getUsername());
        bean.setPassword("newPassword1".toCharArray());
        assertTrue(passwordChangeService.change(bean));
        assertFalse(passwordHistoryService.fetch(c.getUsername()).isEmpty());
        assertFalse(passwordChangeService.change(bean));
    }

    @Test
    void verifySecurityQuestions() throws Throwable {
        val query = PasswordManagementQuery.builder().username("casuser").build();
        query.securityQuestion("Q1", "A1");
        passwordChangeService.updateSecurityQuestions(query);
        assertFalse(passwordChangeService.getSecurityQuestions(query).isEmpty());
    }

    @Test
    void verifyUnlockAccount() throws Throwable {
        val locked = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("locked");
        assertTrue(passwordChangeService.unlockAccount(locked));
    }

    @BeforeEach
    void before() {
        this.jdbcPasswordManagementTransactionTemplate.executeWithoutResult(action -> {
            val jdbcTemplate = new JdbcTemplate(this.jdbcPasswordManagementDataSource);
            dropTablesBeforeTest(jdbcTemplate);

            jdbcTemplate.execute("create table pm_table_accounts (userid varchar(255),"
                                 + "password varchar(255), email varchar(255), phone varchar(255), enabled tinyint);");
            jdbcTemplate.execute("insert into pm_table_accounts values ('casuser', 'password', 'casuser@example.org', '1234567890', 1);");
            jdbcTemplate.execute("insert into pm_table_accounts values ('locked', 'password', 'locked@example.org', '1234567890', 0);");
            jdbcTemplate.execute("insert into pm_table_accounts values ('baduser', 'password', '', '', 1);");

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

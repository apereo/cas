package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is tests for {@link QueryDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    DatabaseAuthenticationTestConfiguration.class
})
@DirtiesContext
public class QueryDatabaseAuthenticationHandlerTests {
    private static final String SQL = "SELECT * FROM casusers where username=?";
    private static final String PASSWORD_FIELD = "password";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private static String getSqlInsertStatementToCreateUserAccount(final int i, final String expired, final String disabled) {
        return String.format("insert into casusers (username, password, expired, disabled, phone) values('%s', '%s', '%s', '%s', '%s');",
            "user" + i, "psw" + i, expired, disabled, "123456789");
    }

    @BeforeEach
    public void initialize() throws Exception {
        val c = this.dataSource.getConnection();
        val s = c.createStatement();
        c.setAutoCommit(true);

        s.execute(getSqlInsertStatementToCreateUserAccount(0, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
        for (var i = 0; i < 10; i++) {
            s.execute(getSqlInsertStatementToCreateUserAccount(i, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
        }
        s.execute(getSqlInsertStatementToCreateUserAccount(20, Boolean.TRUE.toString(), Boolean.FALSE.toString()));
        s.execute(getSqlInsertStatementToCreateUserAccount(21, Boolean.FALSE.toString(), Boolean.TRUE.toString()));

        c.close();
    }

    @AfterEach
    public void afterEachTest() throws Exception {
        val c = this.dataSource.getConnection();
        val s = c.createStatement();
        c.setAutoCommit(true);

        for (var i = 0; i < 5; i++) {
            s.execute("delete from casusers;");
        }
        c.close();
    }

    @Test
    public void verifyAuthenticationFailsToFindUser() throws Exception {
        val q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, SQL, PASSWORD_FIELD, null,
            null, new HashMap<>(0));
        this.thrown.expect(AccountNotFoundException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("usernotfound", "psw1"));
    }

    @Test
    public void verifyPasswordInvalid() throws Exception {
        val q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, SQL, PASSWORD_FIELD,
            null, null, new HashMap<>(0));
        this.thrown.expect(FailedLoginException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user1", "psw11"));
    }

    @Test
    public void verifyMultipleRecords() throws Exception {
        val q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, SQL, PASSWORD_FIELD,
            null, null, new HashMap<>(0));
        this.thrown.expect(FailedLoginException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"));
    }

    @Test
    public void verifyBadQuery() throws Exception {
        val q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, SQL.replace("*", "error"),
            PASSWORD_FIELD, null, null, new HashMap<>(0));
        this.thrown.expect(PreventedException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"));
    }

    @Test
    public void verifySuccess() throws Exception {
        val map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(Collections.singletonList("phone:phoneNumber"));
        val q = new QueryDatabaseAuthenticationHandler("", null, null, null,
            this.dataSource, SQL, PASSWORD_FIELD,
            null, null,
            CollectionUtils.wrap(map));
        val result = q.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "psw3"));
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertTrue(result.getPrincipal().getAttributes().containsKey("phoneNumber"));
    }

    @Test
    public void verifyFindUserAndExpired() throws Exception {
        val q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, SQL, PASSWORD_FIELD,
            "expired", null, new HashMap<>(0));
        this.thrown.expect(AccountPasswordMustChangeException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user20", "psw20"));
        throw new AssertionError("Shouldn't get here");
    }

    @Test
    public void verifyFindUserAndDisabled() throws Exception {
        val q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, SQL, PASSWORD_FIELD,
            null, "disabled", new HashMap<>(0));
        this.thrown.expect(AccountDisabledException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user21", "psw21"));
        throw new AssertionError("Shouldn't get here");
    }

    /**
     * This test proves that in case BCRYPT is used authentication using encoded password always fail
     * with FailedLoginException
     *
     * @throws Exception in case encoding fails
     */
    @Test
    public void verifyBCryptFail() throws Exception {
        val encoder = new BCryptPasswordEncoder(8, RandomUtils.getNativeInstance());
        val sql = SQL.replace("*", '\'' + encoder.encode("pswbc1") + "' password");
        val q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, sql, PASSWORD_FIELD,
            null, null, new HashMap<>(0));
        q.setPasswordEncoder(encoder);
        this.thrown.expect(FailedLoginException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "pswbc1"));
    }

    /**
     * This test proves that in case BCRYPT and
     * using raw password test can authenticate
     */
    @Test
    public void verifyBCryptSuccess() throws Exception {
        val encoder = new BCryptPasswordEncoder(6, RandomUtils.getNativeInstance());
        val sql = SQL.replace("*", '\'' + encoder.encode("pswbc2") + "' password");
        val q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, sql, PASSWORD_FIELD,
            null, null, new HashMap<>(0));

        q.setPasswordEncoder(encoder);
        assertNotNull(q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "pswbc2")));
    }

    @Entity(name = "casusers")
    public static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column
        private String username;

        @Column
        private String password;

        @Column
        private String expired;

        @Column
        private String disabled;

        @Column
        private String phone;
    }
}

package org.apereo.cas.adaptors.jdbc;

import com.google.common.collect.Multimap;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * This is tests for {@link QueryDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@ContextConfiguration(locations = {"classpath:/jpaTestApplicationContext.xml"})
public class QueryDatabaseAuthenticationHandlerTests {

    private static final String SQL = "SELECT * FROM casusers where username=?";
    private static final String PASSWORD_FIELD = "password";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Before
    public void setUp() throws Exception {
        final Connection c = this.dataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);

        s.execute(getSqlInsertStatementToCreateUserAccount(0, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
        for (int i = 0; i < 10; i++) {
            s.execute(getSqlInsertStatementToCreateUserAccount(i, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
        }
        s.execute(getSqlInsertStatementToCreateUserAccount(20, Boolean.TRUE.toString(), Boolean.FALSE.toString()));
        s.execute(getSqlInsertStatementToCreateUserAccount(21, Boolean.FALSE.toString(), Boolean.TRUE.toString()));

        c.close();
    }

    @After
    public void tearDown() throws Exception {
        final Connection c = this.dataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);

        for (int i = 0; i < 5; i++) {
            s.execute("delete from casusers;");
        }
        c.close();
    }

    private static String getSqlInsertStatementToCreateUserAccount(final int i, final String expired, final String disabled) {
        return String.format("insert into casusers (username, password, expired, disabled, phone) values('%s', '%s', '%s', '%s', '%s');",
                "user" + i, "psw" + i, expired, disabled, "123456789");
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

    @Test
    public void verifyAuthenticationFailsToFindUser() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, SQL, PASSWORD_FIELD, null,
                null, new HashMap<>(0));
        this.thrown.expect(AccountNotFoundException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("usernotfound", "psw1"));
    }

    @Test
    public void verifyPasswordInvalid() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, SQL, PASSWORD_FIELD,
                null, null, new HashMap<>(0));
        this.thrown.expect(FailedLoginException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user1", "psw11"));
    }

    @Test
    public void verifyMultipleRecords() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, SQL, PASSWORD_FIELD,
                null, null, new HashMap<>(0));
        this.thrown.expect(FailedLoginException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"));
    }

    @Test
    public void verifyBadQuery() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, SQL.replace("*", "error"),
                PASSWORD_FIELD, null, null, new HashMap<>(0));
        this.thrown.expect(PreventedException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"));
    }

    @Test
    public void verifySuccess() throws Exception {
        final Multimap<String, String> map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(Arrays.asList("phone:phoneNumber"));
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler("", null, null, null,
                this.dataSource, SQL, PASSWORD_FIELD,
                null, null,
                CollectionUtils.wrap(map));
        final HandlerResult result = q.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "psw3"));
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertTrue(result.getPrincipal().getAttributes().containsKey("phoneNumber"));
    }

    @Test
    public void verifyFindUserAndExpired() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, SQL, PASSWORD_FIELD,
                "expired", null, new HashMap<>(0));
        this.thrown.expect(AccountPasswordMustChangeException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user20", "psw20"));
        fail("Shouldn't get here");
    }

    @Test
    public void verifyFindUserAndDisabled() throws Exception {
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, SQL, PASSWORD_FIELD,
                null, "disabled", new HashMap<>(0));
        this.thrown.expect(AccountDisabledException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user21", "psw21"));
        fail("Shouldn't get here");
    }

    /**
     * This test proves that in case BCRYPT is used authentication using encoded password always fail
     * with FailedLoginException
     *
     * @throws Exception in case encoding fails
     */
    @Test
    public void verifyBCryptFail() throws Exception {
        final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(8, RandomUtils.getInstanceNative());
        final String sql = SQL.replace("*", "'" + encoder.encode("pswbc1") + "' password");
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, sql, PASSWORD_FIELD,
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
        final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(6, RandomUtils.getInstanceNative());
        final String sql = SQL.replace("*", "'" + encoder.encode("pswbc2") + "' password");
        final QueryDatabaseAuthenticationHandler q = new QueryDatabaseAuthenticationHandler("", null, null, null, this.dataSource, sql, PASSWORD_FIELD,
                null, null, new HashMap<>(0));

        q.setPasswordEncoder(encoder);
        assertNotNull(q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user3", "pswbc2")));
    }
}

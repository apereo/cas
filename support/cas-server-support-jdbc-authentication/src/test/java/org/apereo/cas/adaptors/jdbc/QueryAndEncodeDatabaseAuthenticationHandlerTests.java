package org.apereo.cas.adaptors.jdbc;

import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.util.ByteSource;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.util.transforms.PrefixSuffixPrincipalNameTransformer;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class})
@ContextConfiguration(locations = {"classpath:/jpaTestApplicationContext.xml"})
public class QueryAndEncodeDatabaseAuthenticationHandlerTests {

    private static final String ALG_NAME = "SHA-512";
    private static final String SQL = "SELECT * FROM users where %s";
    private static final int NUM_ITERATIONS = 5;
    private static final String STATIC_SALT = "STATIC_SALT";
    private static final String PASSWORD_FIELD_NAME = "password";
    private static final String EXPIRED_FIELD_NAME = "expired";
    private static final String DISABLED_FIELD_NAME = "disabled";
    private static final String NUM_ITERATIONS_FIELD_NAME = "numIterations";

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

    private static String getSqlInsertStatementToCreateUserAccount(final int i, final String expired, final String disabled) {
        final String psw = genPassword("user" + i, "salt" + i, NUM_ITERATIONS);

        return String.format(
                "insert into users (username, password, salt, numIterations, expired, disabled) values('%s', '%s', '%s', %s, '%s', '%s');",
                "user" + i, psw, "salt" + i, NUM_ITERATIONS, expired, disabled);
    }

    @After
    public void tearDown() throws Exception {
        final Connection c = this.dataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);

        for (int i = 0; i < 5; i++) {
            s.execute("delete from users;");
        }
        c.close();
    }

    @Test
    public void verifyAuthenticationFailsToFindUser() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler("", null, null, null, dataSource, ALG_NAME,
                buildSql(), PASSWORD_FIELD_NAME, "salt", null, null, "ops", 0, "");

        this.thrown.expect(AccountNotFoundException.class);
        this.thrown.expectMessage("test not found with SQL query");

        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test
    public void verifyAuthenticationInvalidSql() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler("", null, null, null, dataSource, ALG_NAME,
                buildSql("makesNoSenseInSql"), PASSWORD_FIELD_NAME, "salt", null, null, "ops", 0, "");

        this.thrown.expect(PreventedException.class);
        this.thrown.expectMessage("SQL exception while executing query for test");

        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test
    public void verifyAuthenticationMultipleAccounts() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler("", null, null, null, dataSource, ALG_NAME,
                buildSql(), PASSWORD_FIELD_NAME, "salt", null, null, "ops", 0, "");

        this.thrown.expect(FailedLoginException.class);
        this.thrown.expectMessage("Multiple records found for user0");

        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "password0"));
    }

    @Test
    public void verifyAuthenticationSuccessful() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler("", null, null, null, dataSource, ALG_NAME,
                buildSql(), PASSWORD_FIELD_NAME, "salt", null, null, NUM_ITERATIONS_FIELD_NAME, 0, STATIC_SALT);

        final UsernamePasswordCredential c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("user1");
        final HandlerResult r = q.authenticate(c);

        assertNotNull(r);
        assertEquals(r.getPrincipal().getId(), "user1");
    }

    @Test
    public void verifyAuthenticationWithExpiredField() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler("", null, null, null, dataSource, ALG_NAME,
                buildSql(), PASSWORD_FIELD_NAME, "salt", EXPIRED_FIELD_NAME, null, NUM_ITERATIONS_FIELD_NAME, 0, STATIC_SALT);

        this.thrown.expect(AccountPasswordMustChangeException.class);
        this.thrown.expectMessage("Password has expired");

        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("user20"));
        fail("Shouldn't get here");
    }

    @Test
    public void verifyAuthenticationWithDisabledField() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler("", null, null, null, dataSource, ALG_NAME,
                buildSql(), PASSWORD_FIELD_NAME, "salt", null, DISABLED_FIELD_NAME, NUM_ITERATIONS_FIELD_NAME, 0, STATIC_SALT);

        this.thrown.expect(AccountDisabledException.class);
        this.thrown.expectMessage("Account has been disabled");

        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("user21"));
        fail("Shouldn't get here");
    }

    @Test
    public void verifyAuthenticationSuccessfulWithAPasswordEncoder() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler("", null, null, null, dataSource, ALG_NAME,
                buildSql(), PASSWORD_FIELD_NAME, "salt", null, null, NUM_ITERATIONS_FIELD_NAME, 0, STATIC_SALT);
        q.setPasswordEncoder(new PasswordEncoder() {
            @Override
            public String encode(final CharSequence password) {
                return password.toString().concat("1");
            }

            @Override
            public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
                return true;
            }
        });

        q.setPrincipalNameTransformer(new PrefixSuffixPrincipalNameTransformer("user", null));
        final HandlerResult r = q.authenticate(
                CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("1", "user"));

        assertNotNull(r);
        assertEquals(r.getPrincipal().getId(), "user1");
    }

    private static String buildSql(final String where) {
        return String.format(SQL, where);
    }

    private static String buildSql() {
        return String.format(SQL, "username=?;");
    }

    private static String genPassword(final String psw, final String salt, final int iter) {
        try {
            final DefaultHashService hash = new DefaultHashService();
            hash.setPrivateSalt(ByteSource.Util.bytes(STATIC_SALT));
            hash.setHashIterations(iter);
            hash.setGeneratePublicSalt(false);
            hash.setHashAlgorithmName(ALG_NAME);

            return hash.computeHash(new HashRequest.Builder().setSource(psw).setSalt(salt).setIterations(iter).build()).toHex();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Entity(name = "users")
    public static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String username;
        private String password;
        private String salt;
        private String expired;
        private String disabled;
        private long numIterations;
    }
}

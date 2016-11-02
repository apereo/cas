package org.apereo.cas.adaptors.jdbc;

import com.google.common.base.Throwables;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.util.ByteSource;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.support.PrefixSuffixPrincipalNameTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Before
    public void setUp() throws Exception {

        final Connection c = this.dataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);

        s.execute(getSqlInsertStatementToCreateUserAccount(0));
        for (int i = 0; i < 10; i++) {
            s.execute(getSqlInsertStatementToCreateUserAccount(i));
        }

        c.close();
    }

    private static String getSqlInsertStatementToCreateUserAccount(final int i) {
        final String psw = genPassword("user" + i, "salt" + i, NUM_ITERATIONS);

        final String sql = String.format(
                "insert into users (username, password, salt, numIterations) values('%s', '%s', '%s', %s);",
                "user" + i, psw, "salt" + i, NUM_ITERATIONS);
        return sql;
    }

    @After
    public void tearDown() throws Exception {
        final Connection c = this.dataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);

        for (int i = 0; i < 5; i++) {
            final String sql = String.format("delete from users;");
            s.execute(sql);
        }
        c.close();
    }

    @Test(expected = AccountNotFoundException.class)
    public void verifyAuthenticationFailsToFindUser() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler();
        q.setDataSource(dataSource);
        q.setAlgorithmName(ALG_NAME);
        q.setSql(buildSql());
        q.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword());

    }

    @Test(expected = PreventedException.class)
    public void verifyAuthenticationInvalidSql() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler();
        q.setDataSource(dataSource);
        q.setAlgorithmName(ALG_NAME);
        q.setSql(buildSql("makesNoSenseInSql"));
        q.authenticate(TestUtils.getCredentialsWithSameUsernameAndPassword());

    }

    @Test(expected = FailedLoginException.class)
    public void verifyAuthenticationMultipleAccounts() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler();
        q.setDataSource(dataSource);
        q.setAlgorithmName(ALG_NAME);
        q.setSql(buildSql());
        q.authenticate(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "password0"));

    }

    @Test
    public void verifyAuthenticationSuccessful() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler();
        q.setDataSource(dataSource);
        q.setAlgorithmName(ALG_NAME);
        q.setSql(buildSql());
        q.setNumberOfIterationsFieldName("numIterations");
        q.setStaticSalt(STATIC_SALT);
        q.setSaltFieldName("salt");

        final UsernamePasswordCredential c = TestUtils.getCredentialsWithSameUsernameAndPassword("user1");
        final HandlerResult r = q.authenticate(c);

        assertNotNull(r);
        assertEquals(r.getPrincipal().getId(), "user1");
    }

    @Test
    public void verifyAuthenticationSuccessfulWithAPasswordEncoder() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q = new QueryAndEncodeDatabaseAuthenticationHandler();
        q.setDataSource(dataSource);
        q.setAlgorithmName(ALG_NAME);
        q.setSql(buildSql());
        q.setNumberOfIterationsFieldName("numIterations");
        q.setStaticSalt(STATIC_SALT);
        q.setSaltFieldName("salt");
        q.setPasswordFieldName("password");
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
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("1", "user"));

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

            final String pswEnc = hash.computeHash(new HashRequest.Builder()
                    .setSource(psw).setSalt(salt).setIterations(iter).build()).toHex();

            return pswEnc;

        } catch (final Exception e) {
            throw Throwables.propagate(e);
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
        private long numIterations;
    }
}

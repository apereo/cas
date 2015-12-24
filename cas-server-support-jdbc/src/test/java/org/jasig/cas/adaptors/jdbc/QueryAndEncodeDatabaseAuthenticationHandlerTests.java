package org.jasig.cas.adaptors.jdbc;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.util.ByteSource;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.handler.PrefixSuffixPrincipalNameTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
public class QueryAndEncodeDatabaseAuthenticationHandlerTests {
    private static final String ALG_NAME = MessageDigestAlgorithms.SHA_512;
    private static final String SQL = "SELECT * FROM users where %s";
    private static final int NUM_ITERATIONS = 5;
    private static final String STATIC_SALT = "STATIC_SALT";

    private DataSource dataSource;


    @Before
    public void setup() throws Exception {
        final ClassPathXmlApplicationContext ctx = new
            ClassPathXmlApplicationContext("classpath:/jpaTestApplicationContext.xml");

        this.dataSource = ctx.getBean("dataSource", DataSource.class);
        final Connection c = this.dataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);

        s.execute(getSqlInsertStatementToCreateUserAccount(0));
        for (int i = 0; i < 10; i++) {
            s.execute(getSqlInsertStatementToCreateUserAccount(i));
        }

        c.close();
    }

    private String getSqlInsertStatementToCreateUserAccount(final int i) {
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
        final QueryAndEncodeDatabaseAuthenticationHandler q =
                new QueryAndEncodeDatabaseAuthenticationHandler(this.dataSource, buildSql(),
                        ALG_NAME);
        q.authenticateUsernamePasswordInternal(TestUtils.getCredentialsWithSameUsernameAndPassword());

    }

    @Test(expected = PreventedException.class)
    public void verifyAuthenticationInvalidSql() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q =
                new QueryAndEncodeDatabaseAuthenticationHandler(this.dataSource, buildSql("makesNoSenseInSql"),
                        ALG_NAME);
        q.authenticateUsernamePasswordInternal(TestUtils.getCredentialsWithSameUsernameAndPassword());

    }

    @Test(expected = FailedLoginException.class)
    public void verifyAuthenticationMultipleAccounts() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q =
                new QueryAndEncodeDatabaseAuthenticationHandler(this.dataSource, buildSql(),
                        ALG_NAME);
        q.authenticateUsernamePasswordInternal(
                TestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "password0"));

    }

    @Test
    public void verifyAuthenticationSuccessful() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q =
                new QueryAndEncodeDatabaseAuthenticationHandler(this.dataSource, buildSql(),
                        ALG_NAME);
        q.setNumberOfIterationsFieldName("numIterations");
        q.setStaticSalt(STATIC_SALT);

        final UsernamePasswordCredential c = TestUtils.getCredentialsWithSameUsernameAndPassword("user1");
        final HandlerResult r = q.authenticateUsernamePasswordInternal(c);

        assertNotNull(r);
        assertEquals(r.getPrincipal().getId(), "user1");
    }

    @Test
    public void verifyAuthenticationSuccessfulWithAPasswordEncoder() throws Exception {
        final QueryAndEncodeDatabaseAuthenticationHandler q =
                new QueryAndEncodeDatabaseAuthenticationHandler(this.dataSource, buildSql(),
                        ALG_NAME);
        q.setNumberOfIterationsFieldName("numIterations");
        q.setStaticSalt(STATIC_SALT);
        q.setPasswordEncoder(new PasswordEncoder() {
            @Override
            public String encode(final String password) {
                return password.concat("1");
            }
        });

        q.setPrincipalNameTransformer(new PrefixSuffixPrincipalNameTransformer("user", null));
        final HandlerResult r = q.authenticateUsernamePasswordInternal(
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


    private String genPassword(final String psw, final String salt, final int iter) {
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
            throw new RuntimeException(e);
        }
    }
    @Entity(name="users")
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

package org.apereo.cas.adaptors.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

/**
 * This is tests for {@link QueryDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    DatabaseAuthenticationTestConfiguration.class
})
@Slf4j
@DirtiesContext
public class NamedQueryDatabaseAuthenticationHandlerTests {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Before
    public void initialize() throws Exception {
        final var c = this.dataSource.getConnection();
        final var s = c.createStatement();
        c.setAutoCommit(true);
        s.execute(getSqlInsertStatementToCreateUserAccount(0, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
        c.close();
    }

    @After
    public void afterEachTest() throws Exception {
        final var c = this.dataSource.getConnection();
        final var s = c.createStatement();
        c.setAutoCommit(true);
        s.execute("delete from casusers;");
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
    public void verifySuccess() throws Exception {
        final var sql = "SELECT * FROM casusers where username=:username";
        final var map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(Arrays.asList("phone:phoneNumber"));
        final var q = new QueryDatabaseAuthenticationHandler("namedHandler",
            null, PrincipalFactoryUtils.newPrincipalFactory(), 0,
            this.dataSource, sql, "password",
            null, null,
            CollectionUtils.wrap(map));
        final var result = q.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"));
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertTrue(result.getPrincipal().getAttributes().containsKey("phoneNumber"));
    }

    @Test
    public void verifySuccessWithCount() throws Exception {
        final var sql = "SELECT count(*) as total FROM casusers where username=:username AND password=:password";
        final var map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(Arrays.asList("phone:phoneNumber"));
        final var q = new QueryDatabaseAuthenticationHandler("namedHandler",
            null, PrincipalFactoryUtils.newPrincipalFactory(), 0,
            this.dataSource, sql, null,
            null, null,
            CollectionUtils.wrap(map));
        final var result = q.authenticate(
            CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "psw0"));
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertFalse(result.getPrincipal().getAttributes().containsKey("phoneNumber"));
    }

    @Test
    public void verifyFailsWithMissingTotalField() throws Exception {
        final var sql = "SELECT count(*) FROM casusers where username=:username AND password=:password";
        final var q = new QueryDatabaseAuthenticationHandler("namedHandler",
            null, PrincipalFactoryUtils.newPrincipalFactory(), 0,
            this.dataSource, sql, null,
            null, null,
            new LinkedHashMap<>());
        thrown.expect(FailedLoginException.class);
        q.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("whatever", "psw0"));
    }
}

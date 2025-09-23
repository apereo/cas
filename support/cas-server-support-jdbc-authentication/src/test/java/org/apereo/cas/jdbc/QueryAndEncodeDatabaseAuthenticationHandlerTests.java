package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryEncodeJdbcAuthenticationProperties;
import org.apereo.cas.jpa.JpaPersistenceProviderContext;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.transforms.PrefixSuffixPrincipalNameTransformer;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@SuppressWarnings("JDBCExecuteWithNonConstantString")
@Tag("JDBCAuthentication")
@Import(QueryAndEncodeDatabaseAuthenticationHandlerTests.DatabaseTestConfiguration.class)
class QueryAndEncodeDatabaseAuthenticationHandlerTests extends BaseDatabaseAuthenticationHandlerTests {
    private static final String ALG_NAME = "SHA-512";

    private static final String SQL = "SELECT * FROM users where %s";

    private static final int NUM_ITERATIONS = 5;

    private static final String STATIC_SALT = "STATIC_SALT";

    private static final String PASSWORD_FIELD_NAME = "password";

    private static final String EXPIRED_FIELD_NAME = "expired";

    private static final String DISABLED_FIELD_NAME = "disabled";

    private static final String NUM_ITERATIONS_FIELD_NAME = "numIterations";

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private static String getSqlInsertStatementToCreateUserAccount(final int i, final String expired, final String disabled) {
        val psw = genPassword("user%d".formatted(i), "salt%d".formatted(i), NUM_ITERATIONS);
        return String.format(
            "insert into users (username, password, salt, numIterations, expired, disabled, location, color) values('%s', '%s', '%s', %s, '%s', '%s', '%s', '%s');",
            "user%d".formatted(i), psw, "salt%d".formatted(i), NUM_ITERATIONS, expired, disabled, "London", "blue");
    }

    private static String buildSql(final String where) {
        return String.format(SQL, where);
    }

    private static String buildSql() {
        return String.format(SQL, "username=?;");
    }

    private static String genPassword(final String psw, final String salt, final int iter) {
        return DigestUtils.rawDigest(ALG_NAME, STATIC_SALT.getBytes(StandardCharsets.UTF_8),
            salt.getBytes(StandardCharsets.UTF_8), psw, iter);
    }

    @BeforeEach
    void initialize() throws Exception {
        try (val connection = this.dataSource.getConnection()) {
            try (val statement = connection.createStatement()) {
                connection.setAutoCommit(true);

                statement.execute(getSqlInsertStatementToCreateUserAccount(0, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
                for (var i = 0; i < 10; i++) {
                    statement.execute(getSqlInsertStatementToCreateUserAccount(i, Boolean.FALSE.toString(), Boolean.FALSE.toString()));
                }
                statement.execute(getSqlInsertStatementToCreateUserAccount(20, Boolean.TRUE.toString(), Boolean.FALSE.toString()));
                statement.execute(getSqlInsertStatementToCreateUserAccount(21, Boolean.FALSE.toString(), Boolean.TRUE.toString()));
            }
        }
    }

    @AfterEach
    public void afterEachTest() throws Exception {
        try (val connection = this.dataSource.getConnection()) {
            try (val statement = connection.createStatement()) {
                connection.setAutoCommit(true);
                statement.execute("delete from users;");
            }
        }
    }

    @Test
    void verifyAuthenticationFailsToFindUser() {
        val properties = new QueryEncodeJdbcAuthenticationProperties().setAlgorithmName(ALG_NAME)
            .setSql(buildSql()).setPasswordFieldName(PASSWORD_FIELD_NAME)
            .setSaltFieldName("salt").setDisabledFieldName("ops");
        val handler = getAuthenticationHandler(properties);
        assertThrows(AccountNotFoundException.class, () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));
    }

    @Test
    void verifyAuthenticationInvalidSql() {
        val properties = new QueryEncodeJdbcAuthenticationProperties().setAlgorithmName(ALG_NAME)
            .setSql(buildSql("makesNoSenseInSql")).setPasswordFieldName(PASSWORD_FIELD_NAME)
            .setSaltFieldName("salt").setDisabledFieldName("ops");
        val handler = getAuthenticationHandler(properties);
        assertThrows(PreventedException.class, () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), mock(Service.class)));
    }

    @Test
    void verifyAuthenticationMultipleAccounts() {
        val properties = new QueryEncodeJdbcAuthenticationProperties().setAlgorithmName(ALG_NAME)
            .setSql(buildSql()).setPasswordFieldName(PASSWORD_FIELD_NAME)
            .setSaltFieldName("salt").setDisabledFieldName("ops");
        val handler = getAuthenticationHandler(properties);

        assertThrows(FailedLoginException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("user0", "password0"), mock(Service.class)));
    }

    @Test
    void verifyAuthenticationSuccessful() throws Throwable {
        val properties = new QueryEncodeJdbcAuthenticationProperties().setAlgorithmName(ALG_NAME)
            .setSql(buildSql()).setPasswordFieldName(PASSWORD_FIELD_NAME)
            .setSaltFieldName("salt").setDisabledFieldName("ops")
            .setNumberOfIterationsFieldName(NUM_ITERATIONS_FIELD_NAME).
            setStaticSalt(STATIC_SALT);
        val handler = getAuthenticationHandler(properties);
        val credentials = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("user1");
        val result = handler.authenticate(credentials, mock(Service.class));
        assertNotNull(result);
        assertEquals("user1", result.getPrincipal().getId());
    }

    @Test
    void verifyAuthenticationWithExpiredField() {
        val properties = new QueryEncodeJdbcAuthenticationProperties().setAlgorithmName(ALG_NAME)
            .setSql(buildSql())
            .setPasswordFieldName(PASSWORD_FIELD_NAME)
            .setExpiredFieldName(EXPIRED_FIELD_NAME)
            .setStaticSalt(STATIC_SALT)
            .setSaltFieldName("salt");
        val handler = getAuthenticationHandler(properties);
        assertThrows(AccountPasswordMustChangeException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("user20"), mock(Service.class)));
    }

    @Test
    void verifyAuthenticationWithDisabledField() {
        val properties = new QueryEncodeJdbcAuthenticationProperties().setAlgorithmName(ALG_NAME)
            .setSql(buildSql()).setPasswordFieldName(PASSWORD_FIELD_NAME)
            .setDisabledFieldName(DISABLED_FIELD_NAME)
            .setStaticSalt(STATIC_SALT)
            .setSaltFieldName("salt");
        val handler = getAuthenticationHandler(properties);
        assertThrows(AccountDisabledException.class,
            () -> handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("user21"), mock(Service.class)));
    }

    @Test
    void verifyAuthenticationSuccessfulWithAPasswordEncoder() throws Throwable {
        val properties = new QueryEncodeJdbcAuthenticationProperties()
            .setAlgorithmName(ALG_NAME)
            .setSql(buildSql()).setPasswordFieldName(PASSWORD_FIELD_NAME)
            .setNumberOfIterationsFieldName(NUM_ITERATIONS_FIELD_NAME)
            .setStaticSalt(STATIC_SALT)
            .setSaltFieldName("salt");
        properties.setPrincipalAttributeList(List.of("location", "color"));
        val handler = getAuthenticationHandler(properties);
        handler.setPasswordEncoder(new PasswordEncoder() {
            @Override
            public String encode(final CharSequence password) {
                return password.toString().concat("1");
            }

            @Override
            public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
                return true;
            }
        });
        handler.setPrincipalNameTransformer(new PrefixSuffixPrincipalNameTransformer("user", null));
        val result = handler.authenticate(CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("1", "user"), mock(Service.class));
        assertNotNull(result);
        assertEquals("user1", result.getPrincipal().getId());
        assertEquals("blue", result.getPrincipal().getAttributes().get("color").getFirst());
        assertEquals("London", result.getPrincipal().getAttributes().get("location").getFirst());
    }

    private QueryAndEncodeDatabaseAuthenticationHandler getAuthenticationHandler(final QueryEncodeJdbcAuthenticationProperties properties) {
        return (QueryAndEncodeDatabaseAuthenticationHandler) JdbcAuthenticationUtils.newAuthenticationHandler(properties, applicationContext,
            PrincipalFactoryUtils.newPrincipalFactory(), new PasswordPolicyContext(), dataSource);
    }

    @TestConfiguration(value = "TestConfiguration", proxyBeanMethods = false)
    static class DatabaseTestConfiguration {
        @Bean
        public JpaPersistenceProviderContext persistenceProviderContext() {
            return new JpaPersistenceProviderContext().setIncludeEntityClasses(
                Set.of(QueryAndEncodeDatabaseAuthenticationHandlerTests.UsersTable.class.getName()));
        }
    }


    @SuppressWarnings("unused")
    @Entity(name = "users")
    static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String username;

        private String password;

        private String location;

        private String color;

        private String salt;

        private String expired;

        private String disabled;

        private long numIterations;
    }
}

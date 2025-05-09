package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasJdbcAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasJdbcAuthenticationConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(
    classes = CasJdbcAuthenticationConfigurationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.jdbc.encode[0].sql=SELECT * FROM users WHERE uid=?",

        "cas.authn.jdbc.query[0].password-encoder.type=DEFAULT",
        "cas.authn.jdbc.query[0].password-encoder.encoding-algorithm=SHA-256",
        "cas.authn.jdbc.query[0].sql=SELECT * FROM users WHERE uid=?",
        "cas.authn.jdbc.query[0].field-password=psw",
        "cas.authn.jdbc.query[0].credential-criteria=.*",
        "cas.authn.jdbc.query[0].user=sa",
        "cas.authn.jdbc.query[0].password=",
        "cas.authn.jdbc.query[0].driver-class=org.hsqldb.jdbcDriver",
        "cas.authn.jdbc.query[0].url=jdbc:hsqldb:mem:cas-hsql-authn-db",
        "cas.authn.jdbc.query[0].dialect=org.hibernate.dialect.HSQLDialect",
        "cas.authn.jdbc.query[0].field-user=uid",
        "cas.authn.jdbc.query[0].field-password=psw",
        "cas.authn.jdbc.query[0].table-users=custom_users_table",

        "cas.authn.jdbc.search[0].order=1000",
        
        "cas.authn.jdbc.bind[0].name=BindHandler",
        "cas.authn.jdbc.bind[0].order=1000",
        "cas.authn.jdbc.bind[0].driver-class=org.hsqldb.jdbcDriver",
        "cas.authn.jdbc.bind[0].url=jdbc:hsqldb:mem:cas-hsql-authn-db",
        "cas.authn.jdbc.bind[0].dialect=org.hibernate.dialect.HSQLDialect"
    })
@Tag("JDBCAuthentication")
@ExtendWith(CasTestExtension.class)
class CasJdbcAuthenticationConfigurationTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(AuthenticationManager.BEAN_NAME)
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void initialize() throws Exception {
        val props = casProperties.getAuthn().getJdbc().getQuery().getFirst();
        val dataSource = JpaBeans.newDataSource(props.getDriverClass(), props.getUser(),
            props.getPassword(), props.getUrl());

        try (val connection = dataSource.getConnection()) {
            try (val statement = connection.createStatement()) {
                connection.setAutoCommit(true);
                statement.execute("CREATE TABLE IF NOT EXISTS users (id INT NOT NULL, uid VARCHAR(50), psw VARCHAR(512));");
                statement.execute("INSERT INTO users VALUES (1, 'casuser', '" + DigestUtils.sha256("Mellon") + "');");
            }
        }
    }

    @Test
    void verifyOperation() throws Throwable {
        val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(CoreAuthenticationTestUtils.getService(), credential);
        val result = authenticationManager.authenticate(transaction);
        assertNotNull(result);
    }

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasHibernateJpaAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreMultitenancyAutoConfiguration.class,
        CasCoreEnvironmentBootstrapAutoConfiguration.class,
        CasJdbcAuthenticationAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import(CasRegisteredServicesTestConfiguration.class)
    public static class SharedTestConfiguration {
    }

}

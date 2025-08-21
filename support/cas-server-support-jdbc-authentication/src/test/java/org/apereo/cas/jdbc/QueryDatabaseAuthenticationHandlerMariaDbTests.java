package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryJdbcAuthenticationProperties;
import org.apereo.cas.jpa.JpaPersistenceProviderContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.serialization.SerializationUtils;
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
import org.springframework.test.context.TestPropertySource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is postgres tests for {@link QueryDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "database.user=root",
    "database.password=mypass",
    "database.driver-class=org.mariadb.jdbc.Driver",
    "database.name=mysql",
    "database.url=jdbc:mariadb://localhost:3306/",
    "database.dialect=org.hibernate.dialect.MariaDBDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@Tag("MariaDb")
@Import(QueryDatabaseAuthenticationHandlerMariaDbTests.DatabaseTestConfiguration.class)
class QueryDatabaseAuthenticationHandlerMariaDbTests extends BaseDatabaseAuthenticationHandlerTests {
    private static final String SQL = "SELECT * FROM casmariadbusers where username=?";

    private static final String PASSWORD_FIELD = "password";

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @BeforeEach
    void initialize() throws Exception {
        try (val connection = this.dataSource.getConnection()) {
            connection.setAutoCommit(true);
            try (val pstmt = connection.prepareStatement("insert into casmariadbusers (username, password, location) values(?,?,?);")) {
                pstmt.setString(1, "casuser");
                pstmt.setString(2, "Mellon");
                pstmt.setString(3, "earth");
                pstmt.executeUpdate();
            }
        }
    }

    @AfterEach
    public void afterEachTest() throws Exception {
        try (val c = this.dataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute("delete from casmariadbusers;");
            }
        }
    }

    @Test
    void verifySuccess() throws Throwable {
        val properties = new QueryJdbcAuthenticationProperties().setSql(SQL).setFieldPassword(PASSWORD_FIELD).setFieldDisabled("disabled");
        properties.setPrincipalAttributeList(List.of("location"));
        val q = new QueryDatabaseAuthenticationHandler(properties, PrincipalFactoryUtils.newPrincipalFactory(), dataSource);
        val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = q.authenticate(credential, mock(Service.class));
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertTrue(result.getPrincipal().getAttributes().containsKey("location"));
        assertNotNull(SerializationUtils.serialize(result));
    }


    @TestConfiguration(value = "TestConfiguration", proxyBeanMethods = false)
    static class DatabaseTestConfiguration {
        @Bean
        public JpaPersistenceProviderContext persistenceProviderContext() {
            return new JpaPersistenceProviderContext().setIncludeEntityClasses(Set.of(QueryDatabaseAuthenticationHandlerMariaDbTests.UsersTable.class.getName()));
        }
    }

    @SuppressWarnings("unused")
    @Entity(name = "casmariadbusers")
    static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column
        private String username;

        @Column
        private String password;

        @Column
        private String location;
    }
}

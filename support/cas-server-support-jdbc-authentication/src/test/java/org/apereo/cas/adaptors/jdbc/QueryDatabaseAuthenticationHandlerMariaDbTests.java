package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryJdbcAuthenticationProperties;
import org.apereo.cas.jpa.JpaPersistenceProviderContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.sql.DataSource;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
    "database.dialect=org.hibernate.dialect.MariaDB103Dialect"
})
@EnabledIfPortOpen(port = 3306)
@Tag("MariaDb")
@Import(QueryDatabaseAuthenticationHandlerMariaDbTests.DatabaseTestConfiguration.class)
public class QueryDatabaseAuthenticationHandlerMariaDbTests extends BaseDatabaseAuthenticationHandlerTests {
    private static final String SQL = "SELECT * FROM casmariadbusers where username=?";

    private static final String PASSWORD_FIELD = "password";

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @BeforeEach
    public void initialize() throws Exception {
        try (val c = this.dataSource.getConnection()) {
            c.setAutoCommit(true);
            try (val pstmt = c.prepareStatement("insert into casmariadbusers (username, password, location) values(?,?,?);")) {
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
    public void verifySuccess() throws Exception {
        val map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(List.of("location"));
        val properties = new QueryJdbcAuthenticationProperties().setSql(SQL).setFieldPassword(PASSWORD_FIELD).setFieldDisabled("disabled");
        val q = new QueryDatabaseAuthenticationHandler(properties, null, PrincipalFactoryUtils.newPrincipalFactory(),
            this.dataSource, CollectionUtils.wrap(map));
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = q.authenticate(c);
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertTrue(result.getPrincipal().getAttributes().containsKey("location"));
        assertNotNull(SerializationUtils.serialize(result));
    }


    @TestConfiguration("TestConfiguration")
    public static class DatabaseTestConfiguration {
        @Bean
        public JpaPersistenceProviderContext persistenceProviderContext() {
            return new JpaPersistenceProviderContext().setIncludeEntityClasses(Set.of(QueryDatabaseAuthenticationHandlerMariaDbTests.UsersTable.class.getName()));
        }
    }
    
    @SuppressWarnings("unused")
    @Entity(name = "casmariadbusers")
    public static class UsersTable {
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

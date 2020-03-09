package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.util.serialization.SerializationUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.sql.DataSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is postgres tests for {@link QueryDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "database.user=postgres",
    "database.password=password",
    "database.driverClass=org.postgresql.Driver",
    "database.name=postgres",
    "database.url=jdbc:postgresql://localhost:5432/",
    "database.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
@EnabledIfPortOpen(port = 5432)
@Tag("Postgres")
public class QueryDatabaseAuthenticationHandlerPostgresTests extends BaseDatabaseAuthenticationHandlerTests {
    private static final String SQL = "SELECT * FROM caspgusers where username=?";

    private static final String PASSWORD_FIELD = "password";

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @BeforeEach
    @SneakyThrows
    public void initialize() {
        try (val c = this.dataSource.getConnection()) {
            c.setAutoCommit(true);
            try (val pstmt = c.prepareStatement("insert into caspgusers (username, password, locations) values(?,?,?);")) {
                val array = c.createArrayOf("text", new String[]{"usa", "uk"});
                pstmt.setString(1, "casuser");
                pstmt.setString(2, "Mellon");
                pstmt.setArray(3, array);
                pstmt.executeUpdate();
            }
        }
    }

    @AfterEach
    @SneakyThrows
    public void afterEachTest() {
        try (val c = this.dataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute("delete from caspgusers;");
            }
        }
    }


    @Test
    @SneakyThrows
    public void verifySuccess() {
        val map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(List.of("locations"));
        val q = new QueryDatabaseAuthenticationHandler("DbHandler", null,
            PrincipalFactoryUtils.newPrincipalFactory(), 0,
            this.dataSource, SQL, PASSWORD_FIELD,
            null, null, CollectionUtils.wrap(map));
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = q.authenticate(c);
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertTrue(result.getPrincipal().getAttributes().containsKey("locations"));
        assertNotNull(SerializationUtils.serialize(result));
    }


    @SuppressWarnings("unused")
    @Entity(name = "caspgusers")
    public static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column
        private String username;

        @Column
        private String password;

        @Column(
            name = "locations",
            columnDefinition = "text[]"
        )
        private String[] locations;
    }
}

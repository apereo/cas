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
    "database.user=root",
    "database.password=mypass",
    "database.driverClass=org.mariadb.jdbc.Driver",
    "database.name=mysql",
    "database.url=jdbc:mariadb://localhost:3306/",
    "database.dialect=org.hibernate.dialect.MariaDB103Dialect"
})
@EnabledIfPortOpen(port = 3306)
@Tag("MariaDb")
public class QueryDatabaseAuthenticationHandlerMariaDbTests extends BaseDatabaseAuthenticationHandlerTests {
    private static final String SQL = "SELECT * FROM casmariadbusers where username=?";

    private static final String PASSWORD_FIELD = "password";

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @BeforeEach
    @SneakyThrows
    public void initialize() {
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
    @SneakyThrows
    public void afterEachTest() {
        try (val c = this.dataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute("delete from casmariadbusers;");
            }
        }
    }


    @Test
    @SneakyThrows
    public void verifySuccess() {
        val map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(List.of("location"));
        val q = new QueryDatabaseAuthenticationHandler("DbHandler", null,
            PrincipalFactoryUtils.newPrincipalFactory(), 0,
            this.dataSource, SQL, PASSWORD_FIELD,
            null, null, CollectionUtils.wrap(map));
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = q.authenticate(c);
        assertNotNull(result);
        assertNotNull(result.getPrincipal());
        assertTrue(result.getPrincipal().getAttributes().containsKey("location"));
        assertNotNull(SerializationUtils.serialize(result));
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

package org.apereo.cas.adaptors.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.category.PostgresCategory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.apereo.cas.util.serialization.SerializationUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.sql.DataSource;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * This is postgres tests for {@link QueryDatabaseAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    DatabaseAuthenticationTestConfiguration.class
})
@DirtiesContext
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 5432)
@Category(PostgresCategory.class)
@TestPropertySource(properties = {
    "database.user=postgres",
    "database.password=password",
    "database.driverClass=org.postgresql.Driver",
    "database.name=postgres",
    "database.url=jdbc:postgresql://localhost:5432/",
    "database.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
public class QueryDatabaseAuthenticationHandlerPostgresTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final String SQL = "SELECT * FROM caspgusers where username=?";
    private static final String PASSWORD_FIELD = "password";

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @BeforeEach
    public void initialize() throws Exception {
        val c = this.dataSource.getConnection();
        c.setAutoCommit(true);
        val pstmt = c.prepareStatement("insert into caspgusers (username, password, locations) values(?,?,?);");
        val array = c.createArrayOf("text", new String[]{"usa", "uk"});
        pstmt.setString(1, "casuser");
        pstmt.setString(2, "Mellon");
        pstmt.setArray(3, array);
        pstmt.executeUpdate();
        c.close();
    }

    @AfterEach
    public void afterEachTest() throws Exception {
        val c = this.dataSource.getConnection();
        val s = c.createStatement();
        c.setAutoCommit(true);
        s.execute("delete from caspgusers;");
        c.close();
    }


    @Test
    public void verifySuccess() throws Exception {
        val map = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(Collections.singletonList("locations"));
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


    @Entity(name = "caspgusers")
    public static class UsersTable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column
        private String username;

        @Column
        private String password;

        //@Type(type = "string-array")
        @Column(
            name = "locations",
            columnDefinition = "text[]"
        )
        private String[] locations;
    }
}

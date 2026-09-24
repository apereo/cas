package org.apereo.cas.aup;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.acceptable-usage-policy.jdbc.table-name=aup_table",
    "cas.acceptable-usage-policy.core.aup-attribute-name=accepted"
})
@Tag("JDBC")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcAcceptableUsagePolicyRepositoryTests extends BaseJdbcAcceptableUsagePolicyRepositoryTests {

    private final AtomicInteger identifiers = new AtomicInteger(100);

    @BeforeAll
    void initialize() throws Exception {
        try (val connection = this.acceptableUsagePolicyDataSource.getConnection()) {
            try (val statement = connection.createStatement()) {
                connection.setAutoCommit(true);
                statement.execute("CREATE TABLE aup_table (id int primary key, username varchar(255), accepted boolean)");
            }
        }
    }

    @AfterAll
    public void cleanup() throws Exception {
        try (val connection = this.acceptableUsagePolicyDataSource.getConnection()) {
            try (val statement = connection.createStatement()) {
                connection.setAutoCommit(true);
                statement.execute("DROP TABLE aup_table;");
            }
        }
    }

    @Test
    void verifyRepositoryAction() throws Throwable {
        val username = UUID.randomUUID().toString();
        addUser(username);
        verifyRepositoryAction(username, CollectionUtils.wrap("accepted", "false"));
    }

    @Test
    void determinePrincipalId() throws Throwable {
        val principalId = determinePrincipalId("casuser", CollectionUtils.wrap("accepted", "false"));
        assertEquals("casuser", principalId);
    }

    private void addUser(final String username) throws Exception {
        try (val connection = this.acceptableUsagePolicyDataSource.getConnection()) {
            try (val statement = connection.createStatement()) {
                connection.setAutoCommit(true);
                statement.execute("INSERT INTO aup_table (id, username, accepted) values ("
                                  + identifiers.incrementAndGet() + ", '" + username + "', false);");
            }
        }
    }
}

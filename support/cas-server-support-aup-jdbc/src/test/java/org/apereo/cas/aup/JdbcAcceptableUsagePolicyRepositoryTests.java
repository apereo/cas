package org.apereo.cas.aup;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.acceptable-usage-policy.jdbc.tableName=aup_table",
    "cas.acceptable-usage-policy.aupAttributeName=accepted"
})
@Tag("JDBC")
public class JdbcAcceptableUsagePolicyRepositoryTests extends BaseJdbcAcceptableUsagePolicyRepositoryTests {

    @BeforeEach
    public void initialize() throws Exception {
        try (val c = this.acceptableUsagePolicyDataSource.getObject().getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute("CREATE TABLE aup_table (id int primary key, username varchar(255), accepted boolean)");
                s.execute("INSERT INTO aup_table (id, username, accepted) values (100, 'casuser', false);");
            }
        }
    }
    
    @AfterEach
    public void cleanup() throws Exception {
        try (val c = this.acceptableUsagePolicyDataSource.getObject().getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute("DROP TABLE aup_table;");
            }
        }
    }
    
    @Test
    public void verifyRepositoryAction() {
        verifyRepositoryAction("casuser", CollectionUtils.wrap("accepted", "false"));
    }
    
    @Test
    public void determinePrincipalId() {
        val principalId = determinePrincipalId("casuser", CollectionUtils.wrap("accepted", "false"));
        assertEquals("casuser", principalId);
    }
}

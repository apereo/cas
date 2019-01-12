package org.apereo.cas.aup;

import org.apereo.cas.util.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import java.sql.Connection;
import java.sql.Statement;
import org.junit.After;
import static org.junit.Assert.*;


/**
 * This is {@link JdbcAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(locations = {"classpath:/jdbc-aup.properties"})
public class JdbcAcceptableUsagePolicyRepositoryTests extends BaseJdbcAcceptableUsagePolicyRepositoryTests {

    @Before
    public void setUp() throws Exception {
        final Connection c = this.acceptableUsagePolicyDataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);
        s.execute("CREATE TABLE aup_table (id int primary key, username varchar(255), accepted boolean)");
        s.execute("INSERT INTO aup_table (id, username, accepted) values (100, 'casuser', false);");
        c.close();
    }
    
    @After
    public void tearDown() throws Exception {
        final Connection c = this.acceptableUsagePolicyDataSource.getConnection();
        final Statement s = c.createStatement();
        c.setAutoCommit(true);
        s.execute("DROP TABLE aup_table;");
        c.close();
    }

    @Test
    public void verifyActionWithDefaultConfig() {
        verifyAction("casuser", CollectionUtils.wrap("accepted", "false"));
    }
    
    @Test
    public void determinePrincipalIdWithDefaultConfig() {
        final String principalId = determinePrincipalId("casuser", CollectionUtils.wrap("accepted", "false"));
        assertEquals("casuser", principalId);
    }
    
}

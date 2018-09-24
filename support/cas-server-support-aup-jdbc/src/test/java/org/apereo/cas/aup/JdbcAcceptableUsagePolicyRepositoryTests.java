package org.apereo.cas.aup;

import org.apereo.cas.config.CasAcceptableUsagePolicyJdbcConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;

import lombok.val;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

/**
 * This is {@link JdbcAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasAcceptableUsagePolicyJdbcConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasPersonDirectoryTestConfiguration.class
})
@TestPropertySource(properties = {
    "cas.acceptableUsagePolicy.jdbc.tableName=aup_table",
    "cas.acceptableUsagePolicy.aupAttributeName=accepted"
    })
public class JdbcAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {
    @Autowired
    @Qualifier("acceptableUsagePolicyDataSource")
    private DataSource acceptableUsagePolicyDataSource;

    @Override
    public boolean hasLiveUpdates() {
        return false;
    }

    @Before
    public void initialize() throws Exception {
        try (val c = this.acceptableUsagePolicyDataSource.getConnection()) {
            try (val s = c.createStatement()) {
                c.setAutoCommit(true);
                s.execute("CREATE TABLE aup_table (id int primary key, username varchar(255), accepted boolean)");
                s.execute("INSERT INTO aup_table (id, username, accepted) values (100, 'casuser', false);");
            }
        }
    }
}

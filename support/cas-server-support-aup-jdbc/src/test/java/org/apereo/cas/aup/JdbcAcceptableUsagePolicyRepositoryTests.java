package org.apereo.cas.aup;

import org.apereo.cas.config.CasAcceptableUsagePolicyJdbcConfiguration;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

/**
 * This is {@link JdbcAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Import(CasAcceptableUsagePolicyJdbcConfiguration.class)
@TestPropertySource(properties = {
    "cas.acceptableUsagePolicy.jdbc.tableName=aup_table",
    "cas.acceptableUsagePolicy.aupAttributeName=accepted"
})
@Getter
public class JdbcAcceptableUsagePolicyRepositoryTests extends BaseAcceptableUsagePolicyRepositoryTests {
    @Autowired
    @Qualifier("acceptableUsagePolicyDataSource")
    private DataSource acceptableUsagePolicyDataSource;

    @Autowired
    @Qualifier("acceptableUsagePolicyRepository")
    private AcceptableUsagePolicyRepository acceptableUsagePolicyRepository;

    @Override
    public boolean hasLiveUpdates() {
        return false;
    }

    @BeforeEach
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

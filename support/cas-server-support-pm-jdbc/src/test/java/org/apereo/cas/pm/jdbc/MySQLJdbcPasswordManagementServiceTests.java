package org.apereo.cas.pm.jdbc;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLJdbcPasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.pm.jdbc.user=root",
    "cas.authn.pm.jdbc.password=password",
    "cas.authn.pm.jdbc.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.authn.pm.jdbc.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.pm.jdbc.dialect=org.hibernate.dialect.MySQL57InnoDBDialect"
})
@EnabledIfPortOpen(port = 3306)
@Tag("MySQL")
public class MySQLJdbcPasswordManagementServiceTests extends JdbcPasswordManagementServiceTests {
    @Override
    protected void dropTablesBeforeTest(final JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("drop table if exists `pm_table_accounts`;");
        jdbcTemplate.execute("drop table if exists `pm_table_questions`;");
    }
}

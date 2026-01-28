package org.apereo.cas.pm.jdbc;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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
    "cas.authn.pm.jdbc.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.authn.pm.jdbc.url=jdbc:mysql://localhost:3306/cas?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.pm.jdbc.dialect=org.hibernate.dialect.MySQLDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@Tag("MySQL")
class MySQLJdbcPasswordManagementServiceTests extends JdbcPasswordManagementServiceTests {
    @Override
    protected void dropTablesBeforeTest(final JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("drop table if exists `pm_table_accounts`;");
        jdbcTemplate.execute("drop table if exists `pm_table_questions`;");
    }
}

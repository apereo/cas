package org.apereo.cas.pm.jdbc;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLJdbcPasswordHistoryServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.authn.pm.jdbc.user=root",
    "cas.authn.pm.jdbc.password=password",
    "cas.authn.pm.jdbc.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.authn.pm.jdbc.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.pm.jdbc.dialect=org.hibernate.dialect.MySQLDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@Tag("MySQL")
class MySQLJdbcPasswordHistoryServiceTests extends JdbcPasswordHistoryServiceTests {

    @BeforeEach
    void initialize() throws Throwable {
        passwordHistoryService.removeAll();
    }
}

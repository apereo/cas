package org.apereo.cas.audit;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MariaDbJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=true",
    "cas.audit.jdbc.ddl-auto=create",
    "cas.audit.jdbc.user=root",
    "cas.audit.jdbc.password=mypass",
    "cas.audit.jdbc.driver-class=org.mariadb.jdbc.Driver",
    "cas.audit.jdbc.url=jdbc:mariadb://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.audit.jdbc.dialect=org.hibernate.dialect.MariaDBDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@Tag("MariaDb")
class MariaDbJdbcAuditConfigurationTests extends CasJdbcAuditConfigurationTests {
}

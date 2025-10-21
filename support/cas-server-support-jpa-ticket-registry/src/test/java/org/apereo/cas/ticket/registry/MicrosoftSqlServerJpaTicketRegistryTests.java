package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link JpaTicketRegistry} class.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = {
    "cas.ticket.registry.jpa.user=sa",
    "cas.ticket.registry.jpa.password=p@ssw0rd",
    "cas.ticket.registry.jpa.driver-class=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "cas.ticket.registry.jpa.url=jdbc:sqlserver://localhost:1433;databaseName=tickets;useUnicode=true;characterEncoding=UTF-8;encrypt=false;trustServerCertificate=true",
    "cas.ticket.registry.jpa.dialect=org.hibernate.dialect.SQLServerDialect"
})
@EnabledIfListeningOnPort(port = 1433)
@Tag("MsSqlServer")
class MicrosoftSqlServerJpaTicketRegistryTests extends BaseJpaTicketRegistryTests {
}

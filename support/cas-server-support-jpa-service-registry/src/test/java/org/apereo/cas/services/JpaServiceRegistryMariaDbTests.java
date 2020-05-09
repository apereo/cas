package org.apereo.cas.services;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Handles tests for {@link JpaServiceRegistry}
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.service-registry.jpa.user=root",
    "cas.service-registry.jpa.password=mypass",
    "cas.service-registry.jpa.driverClass=org.mariadb.jdbc.Driver",
    "cas.service-registry.jpa.url=jdbc:mariadb://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.service-registry.jpa.dialect=org.hibernate.dialect.MariaDB103Dialect"
})
@EnabledIfPortOpen(port = 3306)
@Tag("MariaDb")
public class JpaServiceRegistryMariaDbTests extends JpaServiceRegistryTests {
}

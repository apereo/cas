package org.apereo.cas.services;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
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
    "cas.serviceRegistry.jpa.user=root",
    "cas.serviceRegistry.jpa.password=mypass",
    "cas.serviceRegistry.jpa.driverClass=org.mariadb.jdbc.Driver",
    "cas.serviceRegistry.jpa.url=jdbc:mariadb://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.serviceRegistry.jpa.dialect=org.hibernate.dialect.MariaDB103Dialect"
})
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 3306)
@Tag("MariaDb")
public class JpaServiceRegistryMariaDbTests extends JpaServiceRegistryTests {
}

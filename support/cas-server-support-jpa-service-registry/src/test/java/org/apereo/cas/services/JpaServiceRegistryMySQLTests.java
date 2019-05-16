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
    "cas.serviceRegistry.jpa.password=password",
    "cas.serviceRegistry.jpa.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.serviceRegistry.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.serviceRegistry.jpa.dialect=org.hibernate.dialect.MySQL57InnoDBDialect"
})
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 3306)
@Tag("MySQL")
public class JpaServiceRegistryMySQLTests extends JpaServiceRegistryTests {
}

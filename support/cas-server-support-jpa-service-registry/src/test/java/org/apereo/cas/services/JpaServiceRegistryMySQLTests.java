package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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
    "cas.service-registry.jpa.password=password",
    "cas.service-registry.jpa.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.service-registry.jpa.url=jdbc:mysql://localhost:3306/cas?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.service-registry.jpa.dialect=org.hibernate.dialect.MySQLDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@Tag("MySQL")
class JpaServiceRegistryMySQLTests extends JpaServiceRegistryTests {
}

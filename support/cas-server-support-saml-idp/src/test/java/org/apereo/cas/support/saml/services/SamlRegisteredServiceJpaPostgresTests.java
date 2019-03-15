package org.apereo.cas.support.saml.services;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * The {@link SamlRegisteredServiceJpaPostgresTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.serviceRegistry.jpa.user=sa",
    "cas.serviceRegistry.jpa.password=p@ssw0rd",
    "cas.serviceRegistry.jpa.driverClass=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "cas.serviceRegistry.jpa.url=jdbc:sqlserver://localhost:1433;databaseName=saml",
    "cas.serviceRegistry.jpa.dialect=org.hibernate.dialect.SQLServer2012Dialect"
})
@EnabledIfPortOpen(port = 5432)
@EnabledIfContinuousIntegration
@Tag("Postgres")
public class SamlRegisteredServiceJpaPostgresTests extends SamlRegisteredServiceJpaTests {
}

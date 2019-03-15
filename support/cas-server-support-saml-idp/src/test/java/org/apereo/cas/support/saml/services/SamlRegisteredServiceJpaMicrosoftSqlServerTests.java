package org.apereo.cas.support.saml.services;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * The {@link SamlRegisteredServiceJpaMicrosoftSqlServerTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(properties = {
    "cas.serviceRegistry.jpa.user=sa",
    "cas.serviceRegistry.jpa.password=p@ssw0rd",
    "cas.serviceRegistry.jpa.driverClass=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "cas.serviceRegistry.jpa.url=jdbc:sqlserver://localhost:1433;databaseName=saml",
    "cas.serviceRegistry.jpa.dialect=org.hibernate.dialect.SQLServer2012Dialect"
})
@EnabledIfPortOpen(port = 1433)
@EnabledIfContinuousIntegration
@Tag("MsSqlServer")
public class SamlRegisteredServiceJpaMicrosoftSqlServerTests extends SamlRegisteredServiceJpaTests {

}

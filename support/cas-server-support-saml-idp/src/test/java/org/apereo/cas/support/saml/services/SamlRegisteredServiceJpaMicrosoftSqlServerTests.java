package org.apereo.cas.support.saml.services;

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
    "cas.service-registry.jpa.user=sa",
    "cas.service-registry.jpa.password=p@ssw0rd",
    "cas.service-registry.jpa.driverClass=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "cas.service-registry.jpa.url=jdbc:sqlserver://localhost:1433;databaseName=saml",
    "cas.service-registry.jpa.dialect=org.hibernate.dialect.SQLServer2012Dialect"
})
@EnabledIfPortOpen(port = 1433)
@Tag("MsSqlServer")
public class SamlRegisteredServiceJpaMicrosoftSqlServerTests extends SamlRegisteredServiceJpaTests {
}

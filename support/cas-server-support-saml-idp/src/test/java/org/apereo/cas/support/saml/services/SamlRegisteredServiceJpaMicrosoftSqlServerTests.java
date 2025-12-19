package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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
    "cas.service-registry.jpa.driver-class=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "cas.service-registry.jpa.url=jdbc:sqlserver://localhost:1433;databaseName=saml;encrypt=false;trustServerCertificate=true",
    "cas.service-registry.jpa.dialect=org.hibernate.dialect.SQLServerDialect"
})
@EnabledIfListeningOnPort(port = 1433)
@Tag("MsSqlServer")
class SamlRegisteredServiceJpaMicrosoftSqlServerTests extends SamlRegisteredServiceJpaTests {
}

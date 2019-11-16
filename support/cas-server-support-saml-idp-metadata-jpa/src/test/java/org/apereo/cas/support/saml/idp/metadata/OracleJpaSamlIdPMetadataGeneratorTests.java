package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link OracleJpaSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.jdbc.showSql=true",
    "cas.authn.samlIdp.metadata.jpa.user=system",
    "cas.authn.samlIdp.metadata.jpa.password=Oradoc_db1",
    "cas.authn.samlIdp.metadata.jpa.driverClass=oracle.jdbc.driver.OracleDriver",
    "cas.authn.samlIdp.metadata.jpa.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB",
    "cas.authn.samlIdp.metadata.jpa.dialect=org.hibernate.dialect.Oracle12cDialect"
})
@EnabledIfPortOpen(port = 1521)
@EnabledIfContinuousIntegration
@Tag("Oracle")
public class OracleJpaSamlIdPMetadataGeneratorTests extends JpaSamlIdPMetadataGeneratorTests {
}

package org.apereo.cas.support.saml.idp.metadata;

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
    "cas.authn.saml-idp.metadata.jpa.user=system",
    "cas.authn.saml-idp.metadata.jpa.password=Oradoc_db1",
    "cas.authn.saml-idp.metadata.jpa.driverClass=oracle.jdbc.driver.OracleDriver",
    "cas.authn.saml-idp.metadata.jpa.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB",
    "cas.authn.saml-idp.metadata.jpa.dialect=org.hibernate.dialect.Oracle12cDialect"
})
@EnabledIfPortOpen(port = 1521)
@Tag("Oracle")
public class OracleJpaSamlIdPMetadataGeneratorTests extends JpaSamlIdPMetadataGeneratorTests {
}

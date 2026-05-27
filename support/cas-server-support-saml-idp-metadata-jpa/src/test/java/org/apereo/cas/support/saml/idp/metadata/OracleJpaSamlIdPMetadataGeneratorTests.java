package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link OracleJpaSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.authn.saml-idp.metadata.jpa.user=cas",
    "cas.authn.saml-idp.metadata.jpa.password=cas",
    "cas.authn.saml-idp.metadata.jpa.driver-class=oracle.jdbc.driver.OracleDriver",
    "cas.authn.saml-idp.metadata.jpa.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1",
    "cas.authn.saml-idp.metadata.jpa.dialect=org.hibernate.dialect.OracleDialect"
})
@EnabledIfListeningOnPort(port = 1521)
@Tag("Oracle")
class OracleJpaSamlIdPMetadataGeneratorTests extends JpaSamlIdPMetadataGeneratorTests {
}

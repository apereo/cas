package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link JpaSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.jpa.user=system",
    "cas.authn.saml-idp.metadata.jpa.password=Oradoc_db1",
    "cas.authn.saml-idp.metadata.jpa.driver-class=oracle.jdbc.driver.OracleDriver",
    "cas.authn.saml-idp.metadata.jpa.url=jdbc:oracle:thin:@localhost:1521:ORCLCDB",
    "cas.authn.saml-idp.metadata.jpa.dialect=org.hibernate.dialect.Oracle12cDialect"
})
@EnabledIfPortOpen(port = 1521)
@Tag("Oracle")
public class OracleJpaSamlRegisteredServiceMetadataResolverTests extends JpaSamlRegisteredServiceMetadataResolverTests {
}


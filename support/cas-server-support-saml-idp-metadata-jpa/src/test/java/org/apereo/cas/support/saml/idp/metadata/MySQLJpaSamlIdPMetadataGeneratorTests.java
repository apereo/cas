package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLJpaSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.samlIdp.metadata.jpa.user=root",
    "cas.authn.samlIdp.metadata.jpa.password=password",
    "cas.authn.samlIdp.metadata.jpa.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.authn.samlIdp.metadata.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.samlIdp.metadata.jpa.dialect=org.hibernate.dialect.MySQL8Dialect"
})
@EnabledIfPortOpen(port = 3305)
@EnabledIfContinuousIntegration
@Tag("MySQL")
public class MySQLJpaSamlIdPMetadataGeneratorTests extends JpaSamlIdPMetadataGeneratorTests {
}

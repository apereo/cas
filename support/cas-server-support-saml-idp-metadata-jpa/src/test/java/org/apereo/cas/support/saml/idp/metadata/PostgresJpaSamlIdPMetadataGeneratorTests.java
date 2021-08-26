package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link PostgresJpaSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.authn.saml-idp.metadata.jpa.user=postgres",
    "cas.authn.saml-idp.metadata.jpa.password=password",
    "cas.authn.saml-idp.metadata.jpa.driver-class=org.postgresql.Driver",
    "cas.authn.saml-idp.metadata.jpa.url=jdbc:postgresql://localhost:5432/saml",
    "cas.authn.saml-idp.metadata.jpa.dialect=org.hibernate.dialect.PostgreSQL10Dialect"
})
@EnabledIfPortOpen(port = 5432)
@Tag("Postgres")
public class PostgresJpaSamlIdPMetadataGeneratorTests extends JpaSamlIdPMetadataGeneratorTests {
}

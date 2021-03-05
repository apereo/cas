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
    "cas.authn.saml-idp.metadata.jpa.user=root",
    "cas.authn.saml-idp.metadata.jpa.password=password",
    "cas.authn.saml-idp.metadata.jpa.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.authn.saml-idp.metadata.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.saml-idp.metadata.jpa.dialect=org.hibernate.dialect.MySQL57InnoDBDialect"
})
@EnabledIfPortOpen(port = 3306)
@Tag("MySQL")
public class MySQLJpaSamlRegisteredServiceMetadataResolverTests extends JpaSamlRegisteredServiceMetadataResolverTests {
}


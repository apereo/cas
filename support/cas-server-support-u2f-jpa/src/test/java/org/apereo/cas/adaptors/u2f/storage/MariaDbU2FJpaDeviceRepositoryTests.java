package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MariaDbU2FJpaDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.jpa.user=root",
    "cas.authn.saml-idp.metadata.jpa.password=mypass",
    "cas.authn.saml-idp.metadata.jpa.driver-class=org.mariadb.jdbc.Driver",
    "cas.authn.saml-idp.metadata.jpa.url=jdbc:mariadb://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.saml-idp.metadata.jpa.dialect=org.hibernate.dialect.MariaDB103Dialect"
})
@EnabledIfPortOpen(port = 3306)
@Tag("MariaDb")
public class MariaDbU2FJpaDeviceRepositoryTests extends U2FJpaDeviceRepositoryTests {
}

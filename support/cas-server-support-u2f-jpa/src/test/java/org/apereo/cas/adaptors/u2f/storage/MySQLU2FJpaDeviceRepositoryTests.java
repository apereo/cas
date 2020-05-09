package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLU2FJpaDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.jpa.user=root",
    "cas.authn.saml-idp.metadata.jpa.password=password",
    "cas.authn.saml-idp.metadata.jpa.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.authn.saml-idp.metadata.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.saml-idp.metadata.jpa.dialect=org.hibernate.dialect.MySQL8Dialect"
})
@EnabledIfPortOpen(port = 3306)
@Tag("MySQL")
public class MySQLU2FJpaDeviceRepositoryTests extends U2FJpaDeviceRepositoryTests {
    
}

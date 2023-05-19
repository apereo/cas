package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLU2FJpaDeviceRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.mfa.u2f.jpa.user=root",
    "cas.authn.mfa.u2f.jpa.password=password",
    "cas.authn.mfa.u2f.jpa.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.authn.mfa.u2f.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.mfa.u2f.jpa.dialect=org.hibernate.dialect.MySQLDialect"
})
@EnabledIfListeningOnPort(port = 3306)
@Tag("MySQL")
public class MySQLU2FJpaDeviceRepositoryTests extends U2FJpaDeviceRepositoryTests {
    
}


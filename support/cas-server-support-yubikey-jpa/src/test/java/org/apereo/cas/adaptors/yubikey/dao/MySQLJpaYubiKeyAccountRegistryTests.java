package org.apereo.cas.adaptors.yubikey.dao;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLJpaYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnabledIfListeningOnPort(port = 3306)
@Tag("MySQL")
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.authn.mfa.yubikey.jpa.ddl-auto=create-drop",
    "cas.authn.mfa.yubikey.jpa.user=root",
    "cas.authn.mfa.yubikey.jpa.password=password",
    "cas.authn.mfa.yubikey.jpa.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.authn.mfa.yubikey.jpa.url=jdbc:mysql://localhost:3306/cas?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.mfa.yubikey.jpa.dialect=org.hibernate.dialect.MySQLDialect"
})
class MySQLJpaYubiKeyAccountRegistryTests extends JpaYubiKeyAccountRegistryTests {
}

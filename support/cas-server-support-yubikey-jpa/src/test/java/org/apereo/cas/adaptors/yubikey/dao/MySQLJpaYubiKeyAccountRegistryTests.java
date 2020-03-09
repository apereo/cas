package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLJpaYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnabledIfPortOpen(port = 3306)
@Tag("MySQL")
@TestPropertySource(properties = {
    "cas.jdbc.showSql=true",
    "cas.authn.mfa.yubikey.jpa.ddlAuto=create-drop",
    "cas.authn.mfa.yubikey.jpa.user=root",
    "cas.authn.mfa.yubikey.jpa.password=password",
    "cas.authn.mfa.yubikey.jpa.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.authn.mfa.yubikey.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.mfa.yubikey.jpa.dialect=org.hibernate.dialect.MySQL57InnoDBDialect"
})
public class MySQLJpaYubiKeyAccountRegistryTests extends JpaYubiKeyAccountRegistryTests {
}

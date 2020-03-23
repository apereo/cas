package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MicrosoftSQLServerJpaYubiKeyAccountRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnabledIfPortOpen(port = 1433)
@Tag("MsSqlServer")
@TestPropertySource(properties = {
    "cas.jdbc.showSql=true",
    "cas.authn.mfa.yubikey.jpa.user=sa",
    "cas.authn.mfa.yubikey.jpa.password=p@ssw0rd",
    "cas.authn.mfa.yubikey.jpa.driverClass=com.microsoft.sqlserver.jdbc.SQLServerDriver",
    "cas.authn.mfa.yubikey.jpa.url=jdbc:sqlserver://localhost:1433;databaseName=yubikey;useUnicode=true;characterEncoding=UTF-8",
    "cas.authn.mfa.yubikey.jpa.dialect=org.hibernate.dialect.SQLServer2012Dialect"
})
public class MicrosoftSQLServerJpaYubiKeyAccountRegistryTests extends JpaYubiKeyAccountRegistryTests {
}

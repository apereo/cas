package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * Test cases for {@link JpaMultifactorAuthenticationTrustStorage}.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.jdbc.showSql=true",
    "cas.authn.mfa.trusted.jpa.ddlAuto=create-drop",
    "cas.authn.mfa.trusted.jpa.user=root",
    "cas.authn.mfa.trusted.jpa.password=password",
    "cas.authn.mfa.trusted.jpa.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.authn.mfa.trusted.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.authn.mfa.trusted.jpa.dialect=org.hibernate.dialect.MySQL57InnoDBDialect"
})
@EnabledIfPortOpen(port = 3306)
@Tag("MySQL")
public class MySQLJpaMultifactorAuthenticationTrustStorageTests extends JpaMultifactorAuthenticationTrustStorageTests {
}

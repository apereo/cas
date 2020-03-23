package org.apereo.cas.consent;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLJpaConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnabledIfPortOpen(port = 3306)
@Tag("MySQL")
@TestPropertySource(properties = {
    "cas.jdbc.showSql=true",
    "cas.consent.jpa.ddlAuto=create-drop",
    "cas.consent.jpa.user=root",
    "cas.consent.jpa.password=password",
    "cas.consent.jpa.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.consent.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.consent.jpa.dialect=org.hibernate.dialect.MySQL57InnoDBDialect"
})
public class MySQLJpaConsentRepositoryTests extends JpaConsentRepositoryTests {
}

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
    "cas.jdbc.show-sql=false",
    "cas.consent.jpa.ddl-auto=create-drop",
    "cas.consent.jpa.user=root",
    "cas.consent.jpa.password=password",
    "cas.consent.jpa.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.consent.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.consent.jpa.dialect=org.hibernate.dialect.MySQL8Dialect"
})
public class MySQLJpaConsentRepositoryTests extends JpaConsentRepositoryTests {
}

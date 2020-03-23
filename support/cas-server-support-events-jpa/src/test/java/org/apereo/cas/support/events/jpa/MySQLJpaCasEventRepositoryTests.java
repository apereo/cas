package org.apereo.cas.support.events.jpa;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MySQLJpaCasEventRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnabledIfPortOpen(port = 3306)
@Tag("MySQL")
@TestPropertySource(properties = {
    "cas.jdbc.showSql=true",
    "cas.events.jpa.ddlAuto=create-drop",
    "cas.events.jpa.user=root",
    "cas.events.jpa.password=password",
    "cas.events.jpa.driverClass=com.mysql.cj.jdbc.Driver",
    "cas.events.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.events.jpa.dialect=org.hibernate.dialect.MySQL57InnoDBDialect"
})
public class MySQLJpaCasEventRepositoryTests extends JpaCasEventRepositoryTests {
}

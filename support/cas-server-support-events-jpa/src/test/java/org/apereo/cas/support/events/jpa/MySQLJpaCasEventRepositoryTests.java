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
    "cas.jdbc.show-sql=false",
    "cas.events.jpa.ddl-auto=create-drop",
    "cas.events.jpa.user=root",
    "cas.events.jpa.password=password",
    "cas.events.jpa.driver-class=com.mysql.cj.jdbc.Driver",
    "cas.events.jpa.url=jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useSSL=FALSE",
    "cas.events.jpa.dialect=org.hibernate.dialect.MySQL8Dialect"
})
public class MySQLJpaCasEventRepositoryTests extends JpaCasEventRepositoryTests {
}

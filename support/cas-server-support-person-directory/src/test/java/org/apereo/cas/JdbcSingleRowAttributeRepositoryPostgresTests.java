package org.apereo.cas;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.SneakyThrows;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcSingleRowAttributeRepositoryPostgresTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.authn.attributeRepository.jdbc[0].attributes.uid=uid",
    "cas.authn.attributeRepository.jdbc[0].attributes.locations=locations",
    "cas.authn.attributeRepository.jdbc[0].singleRow=true",
    "cas.authn.attributeRepository.jdbc[0].requireAllAttributes=true",
    "cas.authn.attributeRepository.jdbc[0].sql=SELECT * FROM table_users WHERE {0}",
    "cas.authn.attributeRepository.jdbc[0].username=uid",
    "cas.authn.attributeRepository.jdbc[0].user=postgres",
    "cas.authn.attributeRepository.jdbc[0].password=password",
    "cas.authn.attributeRepository.jdbc[0].driverClass=org.postgresql.Driver",
    "cas.authn.attributeRepository.jdbc[0].url=jdbc:postgresql://localhost:5432/postgres",
    "cas.authn.attributeRepository.jdbc[0].dialect=org.hibernate.dialect.PostgreSQL95Dialect",
    "cas.authn.attributeRepository.jdbc[0].ddlAuto=create-drop"
})
@EnabledIfPortOpen(port = 5432)
@Tag("Postgres")
public class JdbcSingleRowAttributeRepositoryPostgresTests extends JdbcSingleRowAttributeRepositoryTests {

    @Override
    @Test
    public void verifySingleRowAttributeRepository() {
        assertNotNull(attributeRepository);
        val person = attributeRepository.getPerson("casuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);
        assertNotNull(person.getAttributes());
        assertFalse(person.getAttributes().isEmpty());
        assertEquals("casuser", person.getAttributeValue("uid"));
        assertFalse(person.getAttributeValues("locations").isEmpty());
    }

    @Override
    @Test
    public void verifyPeopleSingleRowAttributeRepository() {
        assertNotNull(attributeRepository);
        val people = attributeRepository.getPeople(Map.of("username", List.of("casuser")));
        val person = people.iterator().next();
        assertNotNull(person);
        assertNotNull(person.getAttributes());
        assertFalse(person.getAttributes().isEmpty());
        assertEquals("casuser", person.getAttributeValue("uid"));
        assertFalse(person.getAttributeValues("locations").isEmpty());
    }

    @Override
    @SneakyThrows
    public void prepareDatabaseTable(final Statement s) {
        s.execute("create table table_users (uid VARCHAR(255), locations TEXT[]);");
        s.execute("insert into table_users (uid, locations) values('casuser', '{\"usa\", \"uk\"}' );");
    }
}

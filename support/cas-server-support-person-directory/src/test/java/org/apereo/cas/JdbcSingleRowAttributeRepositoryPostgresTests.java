package org.apereo.cas;

import org.apereo.cas.category.PostgresCategory;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.sql.Statement;

import static org.junit.Assert.*;

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
    "cas.authn.attributeRepository.jdbc[0].dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 5432)
@Category(PostgresCategory.class)
public class JdbcSingleRowAttributeRepositoryPostgresTests extends JdbcSingleRowAttributeRepositoryTests {

    @Test
    public void verifySingleRowAttributeRepository() {
        assertNotNull(attributeRepository);
        val person = attributeRepository.getPerson("casuser");
        assertNotNull(person);
        assertNotNull(person.getAttributes());
        assertFalse(person.getAttributes().isEmpty());
        assertTrue(person.getAttributeValue("uid").equals("casuser"));
        assertFalse(person.getAttributeValues("locations").isEmpty());
    }

    @Override
    @SneakyThrows
    public void prepareDatabaseTable(final Statement s) {
        s.execute("create table table_users (uid VARCHAR(255), locations TEXT[]);");
        s.execute("insert into table_users (uid, locations) values('casuser', '{\"usa\", \"uk\"}' );");
    }
}

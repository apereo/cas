package org.apereo.cas;

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
 * This is {@link JdbcSingleRowAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.authn.attributeRepository.jdbc[0].attributes.uid=uid",
    "cas.authn.attributeRepository.jdbc[0].attributes.displayName=displayName",
    "cas.authn.attributeRepository.jdbc[0].attributes.cn=commonName",
    "cas.authn.attributeRepository.jdbc[0].singleRow=true",
    "cas.authn.attributeRepository.jdbc[0].requireAllAttributes=true",
    "cas.authn.attributeRepository.jdbc[0].single-row=true",
    "cas.authn.attributeRepository.jdbc[0].username=uid",
    "cas.authn.attributeRepository.jdbc[0].sql=SELECT * FROM table_users WHERE {0}",
    "cas.authn.attributeRepository.jdbc[0].case-canonicalization=LOWER",
    "cas.authn.attributeRepository.jdbc[0].case-insensitive-query-attributes=username->LOWER,attr1,attr2"
})
@Tag("JDBC")
public class JdbcSingleRowAttributeRepositoryTests extends BaseJdbcAttributeRepositoryTests {

    @Test
    public void verifySingleRowAttributeRepository() {
        assertNotNull(attributeRepository);
        val person = attributeRepository.getPerson("casuser", IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(person);
        assertNotNull(person.getAttributes());
        assertFalse(person.getAttributes().isEmpty());
        assertTrue(person.getAttributeValue("uid").equals("casuser"));
        assertTrue(person.getAttributeValue("displayName").equals("CAS Display Name"));
        assertTrue(person.getAttributeValue("commonName").equals("CAS Common Name"));
    }


    @Test
    public void verifyPeopleSingleRowAttributeRepository() {
        assertNotNull(attributeRepository);
        val people = attributeRepository.getPeople(Map.of("username", List.of("CASUSER")));
        val person = people.iterator().next();
        assertNotNull(person);
        assertNotNull(person.getAttributes());
        assertFalse(person.getAttributes().isEmpty());
        assertEquals("casuser", person.getAttributeValue("uid"));
        assertFalse(person.getAttributeValues("displayName").isEmpty());
        assertFalse(person.getAttributeValues("commonName").isEmpty());
    }
    
    @Override
    @SneakyThrows
    public void prepareDatabaseTable(final Statement s) {
        s.execute("create table table_users (uid VARCHAR(255),displayName VARCHAR(255),cn VARCHAR(255));");
        s.execute("insert into table_users (uid, displayName, cn) values('casuser', 'CAS Display Name', 'CAS Common Name');");
    }
}

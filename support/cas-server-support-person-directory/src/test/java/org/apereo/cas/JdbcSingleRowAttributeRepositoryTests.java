package org.apereo.cas;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.sql.Statement;

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
    "cas.authn.attributeRepository.jdbc[0].sql=SELECT * FROM table_users WHERE {0}",
    "cas.authn.attributeRepository.jdbc[0].username=uid"
})
public class JdbcSingleRowAttributeRepositoryTests extends BaseJdbcAttributeRepositoryTests {

    @Test
    public void verifySingleRowAttributeRepository() {
        assertNotNull(attributeRepository);
        val person = attributeRepository.getPerson("casuser");
        assertNotNull(person);
        assertNotNull(person.getAttributes());
        assertFalse(person.getAttributes().isEmpty());
        assertTrue(person.getAttributeValue("uid").equals("casuser"));
        assertTrue(person.getAttributeValue("displayName").equals("CAS Display Name"));
        assertTrue(person.getAttributeValue("commonName").equals("CAS Common Name"));
    }

    @Override
    @SneakyThrows
    public void prepareDatabaseTable(final Statement s) {
        s.execute("create table table_users (uid VARCHAR(255),displayName VARCHAR(255),cn VARCHAR(255));");
        s.execute("insert into table_users (uid, displayName, cn) values('casuser', 'CAS Display Name', 'CAS Common Name');");
    }
}

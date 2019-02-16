package org.apereo.cas;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcMultiRowAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.authn.attributeRepository.jdbc[0].attributes.nickname=cas_nickname",
    "cas.authn.attributeRepository.jdbc[0].attributes.role_code=cas_role",
    "cas.authn.attributeRepository.jdbc[0].singleRow=false",
    "cas.authn.attributeRepository.jdbc[0].columnMappings.attr_name=attr_value",
    "cas.authn.attributeRepository.jdbc[0].sql=SELECT * FROM table_users WHERE {0}",
    "cas.authn.attributeRepository.jdbc[0].username=uid"
})
public class JdbcMultiRowAttributeRepositoryTests extends BaseJdbcAttributeRepositoryTests {

    @Test
    public void verifyMultiRowAttributeRepository() {
        assertNotNull(attributeRepository);
        val person = attributeRepository.getPerson("casuser");
        assertNotNull(person);
        assertNotNull(person.getAttributes());
        assertFalse(person.getAttributes().isEmpty());
        assertEquals(3, person.getAttributeValues("cas_role").size());
        assertEquals(2, person.getAttributeValues("cas_nickname").size());
    }

    @Override
    @SneakyThrows
    public void prepareDatabaseTable(final Statement s) {
        s.execute("create table table_users (uid VARCHAR(255),attr_name VARCHAR(255),attr_value VARCHAR(255));");
        s.execute("insert into table_users (uid, attr_name, attr_value) values('casuser', 'role_code', 'AL');");
        s.execute("insert into table_users (uid, attr_name, attr_value) values('casuser', 'role_code', 'SF');");
        s.execute("insert into table_users (uid, attr_name, attr_value) values('casuser', 'role_code', 'AZ');");

        s.execute("insert into table_users (uid, attr_name, attr_value) values('casuser', 'nickname', 'CASTest1');");
        s.execute("insert into table_users (uid, attr_name, attr_value) values('casuser', 'nickname', 'CasTest2');");
    }
}

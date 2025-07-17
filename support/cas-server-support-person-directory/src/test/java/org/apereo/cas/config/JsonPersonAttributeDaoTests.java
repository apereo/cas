package org.apereo.cas.config;

import org.apereo.cas.BasePrincipalAttributeRepositoryTests;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.beans.BeanContainer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.HashMap;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonPersonAttributeDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTest(
    classes = BasePrincipalAttributeRepositoryTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.attribute-repository.json[0].location=classpath:/json-attribute-repository.json",
        "cas.authn.attribute-repository.json[0].order=0"
    })
@Tag("AttributeRepository")
@ExtendWith(CasTestExtension.class)
class JsonPersonAttributeDaoTests {
    @Autowired
    @Qualifier("jsonAttributeRepositories")
    private BeanContainer<PersonAttributeDao> jsonAttributeRepositories;
    

    @Test
    void verifyOperation() {
        val repository = jsonAttributeRepositories.first();
        assertNotNull(repository.getPossibleUserAttributeNames(PersonAttributeDaoFilter.alwaysChoose()));
        assertNotNull(repository.getAvailableQueryAttributes(PersonAttributeDaoFilter.alwaysChoose()));

        val queryMap = new HashMap<String, List<Object>>();
        queryMap.put("username", List.of(PersonAttributeDao.WILDCARD));
        assertEquals(3, repository.getPeopleWithMultivaluedAttributes(queryMap).size());
    }
}

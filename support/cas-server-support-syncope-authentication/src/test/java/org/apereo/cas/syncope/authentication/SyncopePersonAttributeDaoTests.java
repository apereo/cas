package org.apereo.cas.syncope.authentication;

import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.SyncopePersonDirectoryConfiguration;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.Cleanup;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SyncopePersonAttributeDaoTests}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@SuppressWarnings("unused")
@SpringBootTest(classes = {
    SyncopePersonDirectoryConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    BaseSyncopeTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.attribute-repository.syncope.url=http://localhost:8095",
    "cas.authn.attribute-repository.syncope.search-filter=username=={user}"
})
@Nested
@Tag("Authentication")
public class SyncopePersonAttributeDaoTests extends BaseSyncopeTests {

    @Autowired
    @Qualifier("syncopePersonAttributeDaos")
    private BeanContainer<IPersonAttributeDao> syncopePersonAttributeDaos;

    @Autowired
    @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
    private IPersonAttributeDao attributeRepository;

    @Test
    public void matching() {
        val result = MAPPER.createObjectNode();
        result.putArray("result").add(user());
        @Cleanup("stop")
        val webserver = startMockSever(result);

        assertFalse(syncopePersonAttributeDaos.toList().isEmpty());
        assertFalse(attributeRepository.getPeopleWithMultivaluedAttributes(
                Map.of("username", List.of("casuser")), IPersonAttributeDaoFilter.alwaysChoose()).isEmpty());
    }

    @Test
    public void notFound() {
        val result = MAPPER.createObjectNode();
        result.putArray("result");
        @Cleanup("stop")
        val webserver = startMockSever(result);

        assertTrue(attributeRepository.getPeopleWithMultivaluedAttributes(
                Map.of("anotherProp", List.of("casuser")), IPersonAttributeDaoFilter.alwaysChoose()).isEmpty());
    }
}

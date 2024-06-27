package org.apereo.cas;

import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import org.apereo.cas.web.report.CasPersonDirectoryEndpoint;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasPersonDirectoryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@TestPropertySource(properties = "management.endpoint.personDirectory.enabled=true")
@Import(CasPersonDirectoryEndpointTests.CasPersonDirectoryTestConfiguration.class)
@Tag("ActuatorEndpoint")
class CasPersonDirectoryEndpointTests extends AbstractCasEndpointTests {
    private static PersonAttributes PERSON = new SimplePersonAttributes("casuser", Map.of("phone", List.of("123456789")));

    @Autowired
    @Qualifier("casPersonDirectoryEndpoint")
    private CasPersonDirectoryEndpoint personDirectoryEndpoint;

    @Test
    void verifyOperation() throws Exception {
        var person = personDirectoryEndpoint.showCachedAttributesFor("casuser");
        assertNotNull(person);
        assertEquals("123456789", person.getAttributeValue("phone"));
        PERSON = new SimplePersonAttributes("casuser", Map.of("phone", List.of("99887766")));
        personDirectoryEndpoint.removeCachedAttributesFor("casuser");
        person = personDirectoryEndpoint.showCachedAttributesFor("casuser");
        assertEquals("99887766", person.getAttributeValue("phone"));
    }

    @TestConfiguration(value = "CasPersonDirectoryTestConfiguration", proxyBeanMethods = false)
    static class CasPersonDirectoryTestConfiguration {
        @Bean
        public PersonDirectoryAttributeRepositoryPlanConfigurer testAttributeRepositoryPlanConfigurer() {
            return plan -> plan.registerAttributeRepositories(new MockPersonAttributeDao());
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    private static final class MockPersonAttributeDao implements PersonAttributeDao {
        @Override
        public PersonAttributes getPerson(final String s, final Set<PersonAttributes> set,
                                          final PersonAttributeDaoFilter filter) {
            return PERSON;
        }

        @Override
        public Set<PersonAttributes> getPeople(final Map<String, Object> map, final PersonAttributeDaoFilter filter,
                                               final Set<PersonAttributes> set) {
            return Set.of(PERSON);
        }

        @Override
        public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query, final PersonAttributeDaoFilter filter,
                                                                         final Set<PersonAttributes> resultPeople) {
            return Set.of(PERSON);
        }

        @Override
        public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query, final PersonAttributeDaoFilter filter) {
            return Set.of(PERSON);
        }

        @Override
        public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query) {
            return Set.of(PERSON);
        }

        @Override
        public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query, final Set<PersonAttributes> resultPeople) {
            return Set.of(PERSON);
        }

        @Override
        public Map<String, Object> getTags() {
            return Map.of();
        }
        
        @Override
        public int compareTo(@Nonnull final PersonAttributeDao o) {
            return 0;
        }
    }
}

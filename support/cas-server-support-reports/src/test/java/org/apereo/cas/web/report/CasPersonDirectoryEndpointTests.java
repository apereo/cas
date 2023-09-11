package org.apereo.cas.web.report;

import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.AttributeNamedPersonImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import javax.annotation.Nonnull;
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
public class CasPersonDirectoryEndpointTests extends AbstractCasEndpointTests {
    private static IPersonAttributes PERSON = new AttributeNamedPersonImpl(Map.of("phone", List.of("123456789")));

    @Autowired
    @Qualifier("casPersonDirectoryEndpoint")
    private CasPersonDirectoryEndpoint personDirectoryEndpoint;

    @Test
    void verifyOperation() throws Exception {
        var person = personDirectoryEndpoint.showCachedAttributesFor("casuser");
        assertNotNull(person);
        assertEquals("123456789", person.getAttributeValue("phone"));
        PERSON = new AttributeNamedPersonImpl(Map.of("phone", List.of("99887766")));
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
    private static final class MockPersonAttributeDao implements IPersonAttributeDao {
        @Override
        public IPersonAttributes getPerson(final String s, final Set<IPersonAttributes> set, final IPersonAttributeDaoFilter iPersonAttributeDaoFilter) {
            return PERSON;
        }

        @Override
        public Set<IPersonAttributes> getPeople(final Map<String, Object> map, final IPersonAttributeDaoFilter iPersonAttributeDaoFilter, final Set<IPersonAttributes> set) {
            return Set.of(PERSON);
        }

        @Override
        public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query, final IPersonAttributeDaoFilter filter,
                                                                         final Set<IPersonAttributes> resultPeople) {
            return Set.of(PERSON);
        }

        @Override
        public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query, final IPersonAttributeDaoFilter filter) {
            return Set.of(PERSON);
        }

        @Override
        public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query) {
            return Set.of(PERSON);
        }

        @Override
        public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query, final Set<IPersonAttributes> resultPeople) {
            return Set.of(PERSON);
        }

        @Override
        public Map<String, Object> getTags() {
            return Map.of();
        }

        @Override
        public int compareTo(@Nonnull final IPersonAttributeDao o) {
            return 0;
        }

        @Override
        public boolean equals(final Object o) {
            return o instanceof final MockPersonAttributeDao dao && compareTo(dao) == 0;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
        }
    }
}

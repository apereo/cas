package org.apereo.cas.authentication.principal.cache;

import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Parent class for test cases around {@link PrincipalAttributesRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public abstract class AbstractCachingPrincipalAttributesRepositoryTests {

    private static final String MAIL = "mail";
    protected IPersonAttributeDao dao;

    private Map<String, List<Object>> attributes;

    private final PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    private Principal principal;

    @Before
    public void setUp() {
        attributes = new HashMap<>();
        attributes.put("a1", Arrays.asList("v1", "v2", "v3"));

        List email = new ArrayList<>();
        email.add("final@example.com");
        attributes.put(MAIL, email);

        attributes.put("a6", Arrays.asList("v16", "v26", "v63"));
        attributes.put("a2", Arrays.asList("v4"));
        attributes.put("username", Arrays.asList("uid"));

        this.dao = mock(IPersonAttributeDao.class);
        final IPersonAttributes person = mock(IPersonAttributes.class);
        when(person.getName()).thenReturn("uid");
        when(person.getAttributes()).thenReturn(attributes);
        when(dao.getPerson(any(String.class))).thenReturn(person);

        email = new ArrayList<>();
        email.add("final@school.com");
        this.principal = this.principalFactory.createPrincipal("uid",
                Collections.singletonMap(MAIL, email));
    }

    protected abstract AbstractPrincipalAttributesRepository getPrincipalAttributesRepository(String unit, long duration);

    @Test
    public void checkExpiredCachedAttributes() throws Exception {
        assertEquals(this.principal.getAttributes().size(), 1);
        try (AbstractPrincipalAttributesRepository repository = getPrincipalAttributesRepository(TimeUnit.MILLISECONDS.name(), 100)) {
            assertEquals(repository.getAttributes(this.principal).size(), this.attributes.size());
            assertTrue(repository.getAttributes(this.principal).containsKey(MAIL));
            Thread.sleep(200);
            this.attributes.remove(MAIL);
            assertTrue(repository.getAttributes(this.principal).containsKey("a2"));
            assertFalse(repository.getAttributes(this.principal).containsKey(MAIL));
        }
    }

    @Test
    public void ensureCachedAttributesWithUpdate() throws Exception {
        try (AbstractPrincipalAttributesRepository repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5)) {
            assertEquals(repository.getAttributes(this.principal).size(), this.attributes.size());
            assertTrue(repository.getAttributes(this.principal).containsKey(MAIL));

            attributes.clear();
            assertTrue(repository.getAttributes(this.principal).containsKey(MAIL));
        }
    }

    @Test
    public void verifyMergingStrategyWithNoncollidingAttributeAdder() throws Exception {
        try (AbstractPrincipalAttributesRepository repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5)) {
            repository.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.ADD);
            assertTrue(repository.getAttributes(this.principal).containsKey(MAIL));
            assertEquals(repository.getAttributes(this.principal).get(MAIL).toString(), "final@school.com");
        }
    }

    @Test
    public void verifyMergingStrategyWithReplacingAttributeAdder() throws Exception {
        try (AbstractPrincipalAttributesRepository repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5)) {
            repository.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.REPLACE);
            assertTrue(repository.getAttributes(this.principal).containsKey(MAIL));
            assertEquals(repository.getAttributes(this.principal).get(MAIL).toString(), "final@example.com");
        }
    }

    @Test
    public void verifyMergingStrategyWithMultivaluedAttributeMerger() throws Exception {
        try (AbstractPrincipalAttributesRepository repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5)) {
            repository.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.MULTIVALUED);

            final Object mailAttr = repository.getAttributes(this.principal).get(MAIL);
            assertTrue(mailAttr instanceof List);
            final List<?> values = (List) mailAttr;
            assertTrue(values.contains("final@example.com"));
            assertTrue(values.contains("final@school.com"));
        }
    }
}

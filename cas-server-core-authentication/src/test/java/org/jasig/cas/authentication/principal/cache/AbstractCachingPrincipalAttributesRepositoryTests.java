package org.jasig.cas.authentication.principal.cache;

import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Parent class for test cases around {@link org.jasig.cas.authentication.principal.PrincipalAttributesRepository}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public abstract class AbstractCachingPrincipalAttributesRepositoryTests {
    protected IPersonAttributeDao dao;

    private Map<String, List<Object>> attributes;

    private final PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    private Principal principal;

    @Before
    public void setup() {
        attributes = new HashMap<>();
        attributes.put("a1", new ArrayList(Arrays.asList(new Object[]{"v1", "v2", "v3"})));
        attributes.put("mail", new ArrayList(Arrays.asList(new Object[]{"final@example.com"})));
        attributes.put("a6", new ArrayList(Arrays.asList(new Object[]{"v16", "v26", "v63"})));
        attributes.put("a2", new ArrayList(Arrays.asList(new Object[]{"v4"})));
        attributes.put("username", new ArrayList(Arrays.asList(new Object[]{"uid"})));

        this.dao = mock(IPersonAttributeDao.class);
        final IPersonAttributes person = mock(IPersonAttributes.class);
        when(person.getName()).thenReturn("uid");
        when(person.getAttributes()).thenReturn(attributes);
        when(dao.getPerson(any(String.class))).thenReturn(person);

        this.principal = this.principalFactory.createPrincipal("uid",
                Collections.<String, Object>singletonMap("mail",
                        new ArrayList(Arrays.asList(new Object[]{"final@school.com"}))));
    }

    protected abstract AbstractPrincipalAttributesRepository getPrincipalAttributesRepository(TimeUnit unit, long duration);

    @Test
    public void checkExpiredCachedAttributes() throws Exception {
        assertEquals(this.principal.getAttributes().size(), 1);
        try (final AbstractPrincipalAttributesRepository repository = getPrincipalAttributesRepository(TimeUnit.MILLISECONDS, 100)) {
            assertEquals(repository.getAttributes(this.principal).size(), this.attributes.size());
            assertTrue(repository.getAttributes(this.principal).containsKey("mail"));
            Thread.sleep(200);
            this.attributes.remove("mail");
            assertTrue(repository.getAttributes(this.principal).containsKey("a2"));
            assertFalse(repository.getAttributes(this.principal).containsKey("mail"));
        }
    }

    @Test
    public void ensureCachedAttributesWithUpdate() throws Exception {
        try (final AbstractPrincipalAttributesRepository repository = getPrincipalAttributesRepository(TimeUnit.SECONDS, 5)) {
            assertEquals(repository.getAttributes(this.principal).size(), this.attributes.size());
            assertTrue(repository.getAttributes(this.principal).containsKey("mail"));

            attributes.clear();
            assertTrue(repository.getAttributes(this.principal).containsKey("mail"));
        }
    }

    @Test
    public void verifyMergingStrategyWithNoncollidingAttributeAdder() throws Exception {
        try (final AbstractPrincipalAttributesRepository repository = getPrincipalAttributesRepository(TimeUnit.SECONDS, 5)) {
            repository.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.ADD);

            assertTrue(repository.getAttributes(this.principal).containsKey("mail"));
            assertEquals(repository.getAttributes(this.principal).get("mail").toString(), "final@school.com");
        }
    }

    @Test
    public void verifyMergingStrategyWithReplacingAttributeAdder() throws Exception {
        try (final AbstractPrincipalAttributesRepository repository = getPrincipalAttributesRepository(TimeUnit.SECONDS, 5)) {
            repository.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.REPLACE);

            assertTrue(repository.getAttributes(this.principal).containsKey("mail"));
            assertEquals(repository.getAttributes(this.principal).get("mail").toString(), "final@example.com");
        }
    }

    @Test
    public void verifyMergingStrategyWithMultivaluedAttributeMerger() throws Exception {
        try (final AbstractPrincipalAttributesRepository repository = getPrincipalAttributesRepository(TimeUnit.SECONDS, 5)) {
            repository.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.MULTIVALUED);

            assertTrue(repository.getAttributes(this.principal).get("mail") instanceof List);

            final List<?> values = (List) repository.getAttributes(this.principal).get("mail");
            assertTrue(values.contains("final@example.com"));
            assertTrue(values.contains("final@school.com"));
        }
    }
}

package org.apereo.cas.authentication.principal.cache;

import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalFactory;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    private final PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    private Map<String, List<Object>> attributes;
    private Principal principal;

    @BeforeEach
    public void initialize() {
        attributes = new HashMap<>();
        attributes.put("a1", Arrays.asList("v1", "v2", "v3"));

        var email = new ArrayList<>();
        email.add("final@example.com");
        attributes.put(MAIL, email);

        attributes.put("a6", Arrays.asList("v16", "v26", "v63"));
        attributes.put("a2", Collections.singletonList("v4"));
        attributes.put("username", Collections.singletonList("uid"));

        this.dao = mock(IPersonAttributeDao.class);
        val person = mock(IPersonAttributes.class);
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
        assertEquals(1, this.principal.getAttributes().size());
        try (val repository = getPrincipalAttributesRepository(TimeUnit.MILLISECONDS.name(), 100)) {
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
        try (val repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5)) {
            assertEquals(repository.getAttributes(this.principal).size(), this.attributes.size());
            assertTrue(repository.getAttributes(this.principal).containsKey(MAIL));

            attributes.clear();
            assertTrue(repository.getAttributes(this.principal).containsKey(MAIL));
        }
    }

    @Test
    public void verifyMergingStrategyWithNoncollidingAttributeAdder() throws Exception {
        try (val repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5)) {
            repository.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.ADD);
            assertTrue(repository.getAttributes(this.principal).containsKey(MAIL));
            assertEquals("final@school.com", repository.getAttributes(this.principal).get(MAIL).toString());
        }
    }

    @Test
    public void verifyMergingStrategyWithReplacingAttributeAdder() throws Exception {
        try (val repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5)) {
            repository.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.REPLACE);
            assertTrue(repository.getAttributes(this.principal).containsKey(MAIL));
            assertEquals("final@example.com", repository.getAttributes(this.principal).get(MAIL).toString());
        }
    }

    @Test
    public void verifyMergingStrategyWithMultivaluedAttributeMerger() throws Exception {
        try (val repository = getPrincipalAttributesRepository(TimeUnit.SECONDS.name(), 5)) {
            repository.setMergingStrategy(AbstractPrincipalAttributesRepository.MergingStrategy.MULTIVALUED);

            val mailAttr = repository.getAttributes(this.principal).get(MAIL);
            assertTrue(mailAttr instanceof List);
            val values = (List) mailAttr;
            assertTrue(values.contains("final@example.com"));
            assertTrue(values.contains("final@school.com"));
        }
    }
}

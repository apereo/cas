package org.apereo.cas.services;

import org.apereo.cas.CoreAttributesTestUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.google.common.collect.ArrayListMultimap;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.StubPersonAttributeDao;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Attribute filtering policy tests.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class RegisteredServiceAttributeReleasePolicyTests {

    private static final String ATTR_1 = "attr1";
    private static final String ATTR_2 = "attr2";
    private static final String ATTR_3 = "attr3";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final String NEW_ATTR_1_VALUE = "newAttr1";
    private static final String PRINCIPAL_ID = "principalId";

    @Test
    public void verifyMappedAttributeFilterMappedAttributesIsCaseInsensitive() {
        val policy = new ReturnMappedAttributeReleasePolicy();
        val mappedAttr = ArrayListMultimap.<String, Object>create();
        mappedAttr.put(ATTR_1, NEW_ATTR_1_VALUE);
        policy.setAllowedAttributes(CollectionUtils.wrap(mappedAttr));

        val p = mock(Principal.class);
        val map = new HashMap<String, Object>();
        map.put("ATTR1", VALUE_1);
        when(p.getAttributes()).thenReturn(map);
        when(p.getId()).thenReturn(PRINCIPAL_ID);

        val attr = policy.getAttributes(p,
            CoreAttributesTestUtils.getService(),
            CoreAttributesTestUtils.getRegisteredService());
        assertEquals(1, attr.size());
        assertTrue(attr.containsKey(NEW_ATTR_1_VALUE));
    }

    @Test
    public void verifyAttributeFilterMappedAttributesIsCaseInsensitive() {
        val policy = new ReturnAllowedAttributeReleasePolicy();
        val attrs = new ArrayList<String>();
        attrs.add(ATTR_1);
        attrs.add(ATTR_2);

        policy.setAllowedAttributes(attrs);

        val p = mock(Principal.class);
        val map = new HashMap<String, Object>();
        map.put("ATTR1", VALUE_1);
        map.put("ATTR2", VALUE_2);
        when(p.getAttributes()).thenReturn(map);
        when(p.getId()).thenReturn(PRINCIPAL_ID);

        val attr = policy.getAttributes(p, CoreAttributesTestUtils.getService(),
            CoreAttributesTestUtils.getRegisteredService());
        assertEquals(2, attr.size());
        assertTrue(attr.containsKey(ATTR_1));
        assertTrue(attr.containsKey(ATTR_2));
    }

    @Test
    public void verifyAttributeFilterMappedAttributes() {
        val policy = new ReturnMappedAttributeReleasePolicy();
        val mappedAttr = ArrayListMultimap.<String, Object>create();
        mappedAttr.put(ATTR_1, NEW_ATTR_1_VALUE);

        policy.setAllowedAttributes(CollectionUtils.wrap(mappedAttr));
        val p = mock(Principal.class);

        val map = new HashMap<String, Object>();
        map.put(ATTR_1, VALUE_1);
        map.put(ATTR_2, VALUE_2);
        map.put(ATTR_3, Arrays.asList("v3", "v4"));

        when(p.getAttributes()).thenReturn(map);
        when(p.getId()).thenReturn(PRINCIPAL_ID);

        val attr = policy.getAttributes(p, CoreAttributesTestUtils.getService(),
            CoreAttributesTestUtils.getRegisteredService());
        assertEquals(1, attr.size());
        assertTrue(attr.containsKey(NEW_ATTR_1_VALUE));

        val data = SerializationUtils.serialize(policy);
        val p2 = SerializationUtils.deserializeAndCheckObject(data, ReturnMappedAttributeReleasePolicy.class);
        assertNotNull(p2);
        assertEquals(p2.getAllowedAttributes(), policy.getAllowedAttributes());
    }

    @Test
    public void verifyServiceAttributeFilterAllowedAttributes() {
        val policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList(ATTR_1, ATTR_3));
        val p = mock(Principal.class);

        val map = new HashMap<String, Object>();
        map.put(ATTR_1, VALUE_1);
        map.put(ATTR_2, VALUE_2);
        map.put(ATTR_3, Arrays.asList("v3", "v4"));

        when(p.getAttributes()).thenReturn(map);
        when(p.getId()).thenReturn(PRINCIPAL_ID);

        val attr = policy.getAttributes(p, CoreAttributesTestUtils.getService(),
            CoreAttributesTestUtils.getRegisteredService());
        assertEquals(2, attr.size());
        assertTrue(attr.containsKey(ATTR_1));
        assertTrue(attr.containsKey(ATTR_3));

        val data = SerializationUtils.serialize(policy);
        val p2 =
            SerializationUtils.deserializeAndCheckObject(data, ReturnAllowedAttributeReleasePolicy.class);
        assertNotNull(p2);
        assertEquals(p2.getAllowedAttributes(), policy.getAllowedAttributes());
    }

    @Test
    public void verifyServiceAttributeDenyAllAttributes() {
        val policy = new DenyAllAttributeReleasePolicy();
        val p = mock(Principal.class);
        val map = new HashMap<String, Object>();
        map.put("ATTR1", VALUE_1);
        map.put("ATTR2", VALUE_2);
        when(p.getAttributes()).thenReturn(map);
        when(p.getId()).thenReturn(PRINCIPAL_ID);

        val attr = policy.getAttributes(p, CoreAttributesTestUtils.getService(), CoreAttributesTestUtils.getRegisteredService());
        assertTrue(attr.isEmpty());
    }

    @Test
    public void verifyServiceAttributeFilterAllAttributes() {
        val policy = new ReturnAllAttributeReleasePolicy();
        val p = mock(Principal.class);

        val map = new HashMap<String, Object>();
        map.put(ATTR_1, VALUE_1);
        map.put(ATTR_2, VALUE_2);
        map.put(ATTR_3, Arrays.asList("v3", "v4"));

        when(p.getAttributes()).thenReturn(map);
        when(p.getId()).thenReturn(PRINCIPAL_ID);

        val attr = policy.getAttributes(p, CoreAttributesTestUtils.getService(), CoreAttributesTestUtils.getRegisteredService());
        assertEquals(attr.size(), map.size());

        val data = SerializationUtils.serialize(policy);
        val p2 = SerializationUtils.deserializeAndCheckObject(data, ReturnAllAttributeReleasePolicy.class);
        assertNotNull(p2);
    }

    @Test
    public void checkServiceAttributeFilterAllAttributesWithCachingTurnedOn() {
        val policy = new ReturnAllAttributeReleasePolicy();

        val attributes = new HashMap<String, List<Object>>();
        attributes.put("values", Arrays.asList(new Object[]{"v1", "v2", "v3"}));
        attributes.put("cn", Arrays.asList(new Object[]{"commonName"}));
        attributes.put("username", Arrays.asList(new Object[]{"uid"}));

        val dao = new StubPersonAttributeDao(attributes);
        val person = mock(IPersonAttributes.class);
        when(person.getName()).thenReturn("uid");
        when(person.getAttributes()).thenReturn(attributes);

        val repository =
            new CachingPrincipalAttributesRepository(TimeUnit.MILLISECONDS.name(), 100);
        repository.setAttributeRepository(dao);

        val p = new DefaultPrincipalFactory().createPrincipal("uid",
            Collections.singletonMap("mail", "final@example.com"));

        policy.setPrincipalAttributesRepository(repository);

        val attr = policy.getAttributes(p, CoreAttributesTestUtils.getService(),
            CoreAttributesTestUtils.getRegisteredService());
        assertEquals(attr.size(), attributes.size());
    }
}

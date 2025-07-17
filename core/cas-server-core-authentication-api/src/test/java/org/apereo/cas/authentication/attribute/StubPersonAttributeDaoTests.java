package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testcase for {@link StubPersonAttributeDao}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("AttributeRepository")
class StubPersonAttributeDaoTests {

    private StubPersonAttributeDao testInstance;

    private Map<String, List<Object>> backingMap;


    @BeforeEach
    void setUp() {
        val map = new HashMap<String, List<Object>>();
        map.put("shirtColor", List.of("blue"));
        map.put("phone", List.of("777-7777"));
        this.backingMap = map;
        this.testInstance = new StubPersonAttributeDao();
        this.testInstance.setBackingMap(map);

    }

    @Test
    void testGetPossibleUserAttributeNames() {
        val expectedAttributeNames = new HashSet<String>();
        expectedAttributeNames.add("shirtColor");
        expectedAttributeNames.add("phone");
        var possibleAttributeNames = this.testInstance.getPossibleUserAttributeNames(PersonAttributeDaoFilter.alwaysChoose());
        assertEquals(expectedAttributeNames, possibleAttributeNames);

        var nullBacking = new StubPersonAttributeDao();
        assertEquals(Collections.emptySet(), nullBacking.getPossibleUserAttributeNames(PersonAttributeDaoFilter.alwaysChoose()));
    }

    @Test
    void testGetUserAttributesMap() {
        var resultsSet = this.testInstance.getPeopleWithMultivaluedAttributes(new HashMap<>());
        assertEquals(this.backingMap, resultsSet.iterator().next().getAttributes());
    }

    @Test
    void testGetUserAttributesString() {
        assertEquals(this.backingMap, this.testInstance.getPerson("wombat").getAttributes());
    }
}


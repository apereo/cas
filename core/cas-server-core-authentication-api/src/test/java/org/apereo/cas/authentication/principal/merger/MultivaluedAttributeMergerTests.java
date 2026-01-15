package org.apereo.cas.authentication.principal.merger;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultivaluedAttributeMergerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Tag("Attributes")
class MultivaluedAttributeMergerTests extends AbstractAttributeMergerTests {
    private final AttributeMerger attributeMerger = new MultivaluedAttributeMerger();

    @Test
    void testAddDistinct() {
        val someAttributes = new HashMap<String, List<Object>>();
        someAttributes.put("attName", CollectionUtils.wrapList("AttValue"));
        someAttributes.put("attName2", CollectionUtils.wrapList("attValue2"));

        val secondAttributes = new HashMap<String, List<Object>>();
        secondAttributes.put("attName", CollectionUtils.wrapList("attValue", "atTvAlue", "attrValue1"));
        secondAttributes.put("attName3", CollectionUtils.wrapList("attValue2"));

        val adder = new MultivaluedAttributeMerger();
        adder.setDistinctValues(true);
        val result = adder.mergeAttributes(someAttributes, secondAttributes);

        val expected = new HashMap<String, List<Object>>();
        expected.put("attName", CollectionUtils.wrapList("AttValue", "attrValue1"));
        expected.put("attName2", CollectionUtils.wrapList("attValue2"));
        expected.put("attName3", CollectionUtils.wrapList("attValue2"));

        assertEquals(expected, result);
    }

    @Test
    void testAddEmpty() {
        val someAttributes = new HashMap<String, List<Object>>();
        someAttributes.put("attName", CollectionUtils.wrapList("attValue"));
        someAttributes.put("attName2", CollectionUtils.wrapList("attValue2"));

        val expected = new HashMap<>(someAttributes);

        val result = attributeMerger.mergeAttributes(someAttributes, new HashMap<>());
        assertEquals(expected, result);
    }

    @Test
    void testAddNoncolliding() {
        val someAttributes = new HashMap<String, List<Object>>();
        someAttributes.put("attName", CollectionUtils.wrapList("attValue"));
        someAttributes.put("attName2", CollectionUtils.wrapList("attValue2"));

        val otherAttributes = new HashMap<String, List<Object>>();
        otherAttributes.put("attName3", CollectionUtils.wrapList("attValue3"));
        otherAttributes.put("attName4", CollectionUtils.wrapList("attValue4"));

        val expected = new HashMap<String, List<Object>>();
        expected.putAll(someAttributes);
        expected.putAll(otherAttributes);

        val result = attributeMerger.mergeAttributes(someAttributes, otherAttributes);
        assertEquals(expected, result);
    }

    @Test
    void testColliding() {
        val someAttributes = new HashMap<String, List<Object>>();
        someAttributes.put("attName1", CollectionUtils.wrapList((Object) null));
        someAttributes.put("attName2", CollectionUtils.wrapList("attValue2"));

        someAttributes.put("attName5", CollectionUtils.wrapList((Object) null));
        someAttributes.put("attName6", CollectionUtils.wrapList((Object) null));
        someAttributes.put("attName7", CollectionUtils.wrapList("attValue7"));
        someAttributes.put("attName8", CollectionUtils.wrapList("attValue8.1"));

        someAttributes.put("attName9", CollectionUtils.wrapList((Object) null));
        someAttributes.put("attName10", CollectionUtils.wrapList("attValue10"));
        someAttributes.put("attName11", CollectionUtils.wrapList("attValue11.1", "attValue11.2"));
        someAttributes.put("attName12", CollectionUtils.wrapList("attValue12.1", "attValue12.2"));
        someAttributes.put("attName13", CollectionUtils.wrapList("attValue13.1.1", "attValue13.1.2"));


        val otherAttributes = new HashMap<String, List<Object>>();
        otherAttributes.put("attName3", CollectionUtils.wrapList((Object) null));
        otherAttributes.put("attName4", CollectionUtils.wrapList("attValue4"));

        otherAttributes.put("attName5", CollectionUtils.wrapList((Object) null));
        otherAttributes.put("attName6", CollectionUtils.wrapList("attValue6"));
        otherAttributes.put("attName7", CollectionUtils.wrapList((Object) null));
        otherAttributes.put("attName8", CollectionUtils.wrapList("attValue8.2"));

        otherAttributes.put("attName9", CollectionUtils.wrapList("attValue9.1", "attValue9.2"));
        otherAttributes.put("attName10", CollectionUtils.wrapList("attValue10.1", "attValue10.2"));
        otherAttributes.put("attName11", CollectionUtils.wrapList((Object) null));
        otherAttributes.put("attName12", CollectionUtils.wrapList("attValue12"));
        otherAttributes.put("attName13", CollectionUtils.wrapList("attValue13.2.1", "attValue13.2.2"));


        val expected = new HashMap<String, List<Object>>();
        expected.put("attName1", CollectionUtils.wrapList((Object) null));
        expected.put("attName2", CollectionUtils.wrapList("attValue2"));
        expected.put("attName3", CollectionUtils.wrapList((Object) null));
        expected.put("attName4", CollectionUtils.wrapList("attValue4"));
        expected.put("attName5", CollectionUtils.wrapList(null, null));
        expected.put("attName6", CollectionUtils.wrapList(null, "attValue6"));
        expected.put("attName7", CollectionUtils.wrapList("attValue7", null));
        expected.put("attName8", CollectionUtils.wrapList("attValue8.1", "attValue8.2"));

        expected.put("attName9", CollectionUtils.wrapList(null, "attValue9.1", "attValue9.2"));
        expected.put("attName10", CollectionUtils.wrapList("attValue10", "attValue10.1", "attValue10.2"));

        expected.put("attName11", CollectionUtils.wrapList("attValue11.1", "attValue11.2", null));
        expected.put("attName12", CollectionUtils.wrapList("attValue12.1", "attValue12.2", "attValue12"));
        expected.put("attName13", CollectionUtils.wrapList("attValue13.1.1", "attValue13.1.2", "attValue13.2.1", "attValue13.2.2"));

        val result = attributeMerger.mergeAttributes(someAttributes, otherAttributes);
        result.forEach((k, v) -> {
            val expectedValues = expected.get(k);
            assertTrue(v.containsAll(expectedValues));
        });

    }
}

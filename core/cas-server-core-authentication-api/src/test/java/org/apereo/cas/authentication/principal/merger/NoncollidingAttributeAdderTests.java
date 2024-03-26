package org.apereo.cas.authentication.principal.merger;

import org.apereo.cas.util.CollectionUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


/**
 * This is {@link NoncollidingAttributeAdderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Tag("Attributes")
class NoncollidingAttributeAdderTests extends AbstractAttributeMergerTests {

    private final AttributeMerger attributeMerger = new NoncollidingAttributeAdder();

    @Test
    void testAddEmpty() {
        val someAttributes = new HashMap<String, List<Object>>();
        someAttributes.put("attName", CollectionUtils.wrapList("attValue"));
        someAttributes.put("attName2", CollectionUtils.wrapList("attValue2"));

        val expected = new HashMap<>(someAttributes);

        val result = this.attributeMerger.mergeAttributes(someAttributes, new HashMap<>());
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

        val result = this.attributeMerger.mergeAttributes(someAttributes, otherAttributes);
        assertEquals(expected, result);
    }

    @Test
    void testColliding() {
        val someAttributes = new HashMap<String, List<Object>>();
        someAttributes.put("attName", CollectionUtils.wrapList("attValue"));
        someAttributes.put("attName2", CollectionUtils.wrapList("attValue2"));

        val otherAttributes = new HashMap<String, List<Object>>();
        otherAttributes.put("attName", CollectionUtils.wrapList("attValue3"));
        otherAttributes.put("attName4", CollectionUtils.wrapList("attValue4"));

        val expected = new HashMap<>(someAttributes);
        expected.put("attName4", CollectionUtils.wrapList("attValue4"));
        val result = this.attributeMerger.mergeAttributes(someAttributes, otherAttributes);
        assertEquals(expected, result);
    }

}

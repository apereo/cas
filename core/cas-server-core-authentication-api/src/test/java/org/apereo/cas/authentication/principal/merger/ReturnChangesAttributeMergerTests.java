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
 * This is {@link ReturnChangesAttributeMergerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Tag("Attributes")
class ReturnChangesAttributeMergerTests extends AbstractAttributeMergerTests {
    private final AttributeMerger attributeMerger = new ReturnChangesAttributeMerger();

    @Test
    void testReplacement() {
        val mapOne = new HashMap<String, List<Object>>();
        mapOne.put("aaa", CollectionUtils.wrapList("111"));
        mapOne.put("bbb", CollectionUtils.wrapList("222"));

        val mapTwo = new HashMap<String, List<Object>>();
        mapTwo.put("bbb", CollectionUtils.wrapList("bbb"));
        mapTwo.put("ccc", CollectionUtils.wrapList("333"));
        mapTwo.put("aaa", CollectionUtils.wrapList("678"));

        val result = attributeMerger.mergeAttributes(mapOne, mapTwo);
        assertEquals(mapTwo, result);
    }
}

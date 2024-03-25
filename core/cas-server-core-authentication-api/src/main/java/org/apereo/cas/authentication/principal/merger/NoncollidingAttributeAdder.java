package org.apereo.cas.authentication.principal.merger;

import lombok.val;
import java.util.List;
import java.util.Map;

/**
 * Merger which implements accumulation of Map entries such that entries once
 * established are individually immutable.
 *
 * @author andrew.petro@yale.edu
 * @since 7.1.0
 */
public class NoncollidingAttributeAdder extends BaseAdditiveAttributeMerger {

    @Override
    protected Map<String, List<Object>> mergePersonAttributes(final Map<String, List<Object>> toModify, final Map<String, List<Object>> toConsider) {
        for (val sourceEntry : toConsider.entrySet()) {
            val sourceKey = sourceEntry.getKey();
            if (!toModify.containsKey(sourceKey)) {
                val sourceValue = sourceEntry.getValue();
                toModify.put(sourceKey, sourceValue);
            }
        }
        return toModify;
    }
}

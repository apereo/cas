package org.apereo.cas.authentication.principal.merger;

import module java.base;
import lombok.val;

/**
 * Merger which implements accumulation of Map entries such that entries once
 * established are individually immutable.
 *
 * @author andrew.petro@yale.edu
 * @since 7.1.0
 */
public class NoncollidingAttributeAdder extends BaseAdditiveAttributeMerger {

    @Serial
    private static final long serialVersionUID = 7924308725638148943L;

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

package org.apereo.cas.authentication.principal.merger;

import java.io.Serial;
import java.util.List;
import java.util.Map;

/**
 * Attribute merge strategy whereby considered attributes over-write
 * previously set values for attributes with colliding names.
 *
 * @author andrew.petro@yale.edu
 * @since 7.1.0
 */
public class ReplacingAttributeAdder extends BaseAdditiveAttributeMerger {

    @Serial
    private static final long serialVersionUID = -8281970652173233030L;

    @Override
    protected Map<String, List<Object>> mergePersonAttributes(final Map<String, List<Object>> toModify, final Map<String, List<Object>> toConsider) {
        toModify.putAll(toConsider);
        return toModify;
    }
}

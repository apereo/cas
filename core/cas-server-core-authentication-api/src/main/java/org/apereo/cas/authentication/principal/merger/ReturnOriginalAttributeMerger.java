package org.apereo.cas.authentication.principal.merger;

import java.util.List;
import java.util.Map;

/**
 * This is {@link ReturnOriginalAttributeMerger}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class ReturnOriginalAttributeMerger extends BaseAdditiveAttributeMerger {
    @Override
    protected Map<String, List<Object>> mergePersonAttributes(final Map<String, List<Object>> toModify, final Map<String, List<Object>> toConsider) {
        return toModify;
    }
}

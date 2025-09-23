package org.apereo.cas.authentication.principal.merger;

import java.io.Serial;
import java.util.List;
import java.util.Map;

/**
 * This is {@link ReturnChangesAttributeMerger}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class ReturnChangesAttributeMerger extends BaseAdditiveAttributeMerger {
    @Serial
    private static final long serialVersionUID = 3431760069327680377L;

    @Override
    protected Map<String, List<Object>> mergePersonAttributes(final Map<String, List<Object>> toModify, final Map<String, List<Object>> toConsider) {
        return toConsider;
    }
}

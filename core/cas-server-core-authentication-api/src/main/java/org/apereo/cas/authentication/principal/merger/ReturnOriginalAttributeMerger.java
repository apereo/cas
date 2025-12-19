package org.apereo.cas.authentication.principal.merger;

import module java.base;

/**
 * This is {@link ReturnOriginalAttributeMerger}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class ReturnOriginalAttributeMerger extends BaseAdditiveAttributeMerger {
    @Serial
    private static final long serialVersionUID = 8109322251260396156L;

    @Override
    protected Map<String, List<Object>> mergePersonAttributes(final Map<String, List<Object>> toModify, final Map<String, List<Object>> toConsider) {
        return toModify;
    }
}

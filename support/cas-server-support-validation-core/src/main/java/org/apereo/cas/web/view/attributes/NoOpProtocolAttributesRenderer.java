package org.apereo.cas.web.view.attributes;

import module java.base;
import org.apereo.cas.validation.CasProtocolAttributesRenderer;

/**
 * This is {@link NoOpProtocolAttributesRenderer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class NoOpProtocolAttributesRenderer implements CasProtocolAttributesRenderer {
    /**
     * Static instance.
     */
    public static final CasProtocolAttributesRenderer INSTANCE = new NoOpProtocolAttributesRenderer();

    @Override
    public Collection<String> render(final Map<String, Object> attributes) {
        return new ArrayList<>();
    }
}

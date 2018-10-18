package org.apereo.cas.web.view.attributes;

import org.apereo.cas.validation.CasProtocolAttributesRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * This is {@link NoOpProtocolAttributesRenderer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class NoOpProtocolAttributesRenderer implements CasProtocolAttributesRenderer {
    @Override
    public Collection<String> render(final Map<String, Object> attributes) {
        return new ArrayList<>();
    }
}

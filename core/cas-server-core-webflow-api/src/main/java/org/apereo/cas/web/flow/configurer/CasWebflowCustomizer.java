package org.apereo.cas.web.flow.configurer;

import org.springframework.core.Ordered;

import java.util.List;

/**
 * This is {@link CasWebflowCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface CasWebflowCustomizer extends Ordered {

    /**
     * Gets multifactor webflow attribute mappings.
     *
     * @return the multifactor webflow attribute mappings
     */
    default List<String> getWebflowAttributeMappings() {
        return List.of();
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

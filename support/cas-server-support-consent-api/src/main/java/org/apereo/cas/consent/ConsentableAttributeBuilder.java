package org.apereo.cas.consent;

import org.springframework.core.Ordered;

/**
 * This is {@link ConsentableAttributeBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@FunctionalInterface
public interface ConsentableAttributeBuilder extends Ordered {
    CasConsentableAttribute build(CasConsentableAttribute attribute);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}

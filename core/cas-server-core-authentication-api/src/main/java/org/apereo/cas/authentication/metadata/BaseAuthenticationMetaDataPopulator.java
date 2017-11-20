package org.apereo.cas.authentication.metadata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.springframework.core.Ordered;

/**
 * This is {@link BaseAuthenticationMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {
    private int order = Ordered.HIGHEST_PRECEDENCE;

    public BaseAuthenticationMetaDataPopulator() {
        this(Ordered.HIGHEST_PRECEDENCE);
    }

    public BaseAuthenticationMetaDataPopulator(final int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("order", order)
                .toString();
    }
}

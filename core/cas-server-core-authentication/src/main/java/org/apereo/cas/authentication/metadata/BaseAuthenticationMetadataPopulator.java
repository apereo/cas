package org.apereo.cas.authentication.metadata;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.springframework.core.Ordered;

/**
 * This is {@link BaseAuthenticationMetadataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseAuthenticationMetadataPopulator implements AuthenticationMetaDataPopulator {
    private int order = Ordered.HIGHEST_PRECEDENCE;

    public BaseAuthenticationMetadataPopulator() {
        this(Ordered.HIGHEST_PRECEDENCE);
    }

    public BaseAuthenticationMetadataPopulator(final int order) {
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

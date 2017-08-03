package org.apereo.cas.services.support;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The filter that chains other filters inside it.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RegisteredServiceChainingAttributeFilter implements RegisteredServiceAttributeFilter {

    private static final long serialVersionUID = 903015750234610128L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceChainingAttributeFilter.class);

    private List<RegisteredServiceAttributeFilter> filters = new ArrayList<>();

    public RegisteredServiceChainingAttributeFilter() {
    }

    public List<RegisteredServiceAttributeFilter> getFilters() {
        return filters;
    }

    public void setFilters(final List<RegisteredServiceAttributeFilter> filters) {
        this.filters = filters;
    }

    @Override
    public Map<String, Object> filter(final Map<String, Object> givenAttributes) {
        OrderComparator.sort(this.filters);
        final Map<String, Object> attributes = new HashMap<>();
        filters.forEach(policy -> attributes.putAll(policy.filter(givenAttributes)));
        return attributes;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final RegisteredServiceChainingAttributeFilter rhs = (RegisteredServiceChainingAttributeFilter) obj;
        return new EqualsBuilder()
                .append(this.filters, rhs.filters)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(filters)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("filters", filters)
                .toString();
    }
}

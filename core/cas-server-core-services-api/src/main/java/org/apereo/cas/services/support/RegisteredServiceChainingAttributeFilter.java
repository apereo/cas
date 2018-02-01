package org.apereo.cas.services.support;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.springframework.core.OrderComparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.ToString;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * The filter that chains other filters inside it.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString
@Setter
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class RegisteredServiceChainingAttributeFilter implements RegisteredServiceAttributeFilter {

    private static final long serialVersionUID = 903015750234610128L;

    private List<RegisteredServiceAttributeFilter> filters = new ArrayList<>();

    @Override
    public Map<String, Object> filter(final Map<String, Object> givenAttributes) {
        OrderComparator.sort(this.filters);
        final Map<String, Object> attributes = new HashMap<>();
        filters.forEach(policy -> attributes.putAll(policy.filter(givenAttributes)));
        return attributes;
    }

}

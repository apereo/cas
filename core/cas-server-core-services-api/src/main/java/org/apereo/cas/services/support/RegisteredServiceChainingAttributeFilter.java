package org.apereo.cas.services.support;

import module java.base;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * The filter that chains other filters inside it.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString
@Setter
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class RegisteredServiceChainingAttributeFilter implements RegisteredServiceAttributeFilter {

    @Serial
    private static final long serialVersionUID = 903015750234610128L;

    private List<RegisteredServiceAttributeFilter> filters = new ArrayList<>();

    @Override
    public Map<String, List<Object>> filter(final Map<String, List<Object>> givenAttributes) {
        AnnotationAwareOrderComparator.sort(this.filters);
        val attributes = new HashMap<String, List<Object>>();
        filters
            .stream()
            .map(Unchecked.function(policy -> policy.filter(givenAttributes)))
            .forEach(attributes::putAll);
        return attributes;
    }

}

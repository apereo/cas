package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.RegexUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * This is {@link FullRegexRegisteredServiceMatchingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class FullRegexRegisteredServiceMatchingStrategy implements RegisteredServiceMatchingStrategy {
    @Serial
    private static final long serialVersionUID = -8345895859210185565L;
    
    @Override
    public boolean matches(final RegisteredService registeredService, final String serviceId) {
        return RegexUtils.createPattern(registeredService.getServiceId())
            .matcher(serviceId).matches();
    }
}

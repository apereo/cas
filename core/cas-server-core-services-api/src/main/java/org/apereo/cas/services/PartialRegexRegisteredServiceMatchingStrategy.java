package org.apereo.cas.services;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * This is {@link PartialRegexRegisteredServiceMatchingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PartialRegexRegisteredServiceMatchingStrategy implements RegisteredServiceMatchingStrategy {
    @Serial
    private static final long serialVersionUID = -8345895859210185565L;

    @Override
    public boolean matches(final RegisteredService registeredService, final String serviceId) {
        return registeredService.compileServiceIdPattern().matcher(serviceId).find();
    }
}

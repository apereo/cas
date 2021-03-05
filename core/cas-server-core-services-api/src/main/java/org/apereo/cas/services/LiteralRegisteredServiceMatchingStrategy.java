package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;

/**
 * This is {@link LiteralRegisteredServiceMatchingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@AllArgsConstructor
@Accessors(chain = true)
public class LiteralRegisteredServiceMatchingStrategy implements RegisteredServiceMatchingStrategy {
    private static final long serialVersionUID = -8345895859210185565L;

    private boolean caseInsensitive;

    @Override
    public boolean matches(final RegisteredService registeredService, final String serviceId) {
        val assignedId = registeredService.getServiceId().trim();
        if (this.caseInsensitive) {
            return assignedId.equalsIgnoreCase(serviceId);
        }
        return assignedId.equals(serviceId);
    }
}

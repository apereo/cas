package org.apereo.cas.services;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Strings;

/**
 * This is {@link StartsWithRegisteredServiceMatchingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@AllArgsConstructor
@Accessors(chain = true)
public class StartsWithRegisteredServiceMatchingStrategy implements RegisteredServiceMatchingStrategy {
    @Serial
    private static final long serialVersionUID = -2345895859210185565L;

    private String expectedUrl;

    @Override
    public boolean matches(final RegisteredService registeredService, final String serviceId) {
        return Strings.CI.startsWith(serviceId, expectedUrl);
    }
}

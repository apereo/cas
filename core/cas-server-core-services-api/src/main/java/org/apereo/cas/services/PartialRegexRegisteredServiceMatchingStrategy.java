package org.apereo.cas.services;

import org.apereo.cas.util.RegexUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

import java.util.regex.Pattern;

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
@AllArgsConstructor
public class PartialRegexRegisteredServiceMatchingStrategy implements RegisteredServiceMatchingStrategy {
    private static final long serialVersionUID = -8345895859210185565L;

    @JsonIgnore
    @Transient
    @javax.persistence.Transient
    private transient Pattern servicePattern;

    @Override
    public boolean matches(final RegisteredService registeredService, final String serviceId) {
        if (servicePattern == null) {
            this.servicePattern = RegexUtils.createPattern(registeredService.getServiceId());
        }
        return servicePattern.matcher(serviceId).find();
    }

}

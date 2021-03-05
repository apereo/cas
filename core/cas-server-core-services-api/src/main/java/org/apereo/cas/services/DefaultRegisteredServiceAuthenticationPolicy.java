package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link DefaultRegisteredServiceAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ToString
@Getter
@EqualsAndHashCode
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultRegisteredServiceAuthenticationPolicy implements RegisteredServiceAuthenticationPolicy {
    private static final long serialVersionUID = -6777133646772207331L;

    private Set<String> requiredAuthenticationHandlers = new HashSet<>();
    
    private Set<String> excludedAuthenticationHandlers = new HashSet<>();

    private RegisteredServiceAuthenticationPolicyCriteria criteria = new AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria();
}

package org.apereo.cas.services;

import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.policy.ExcludedAuthenticationHandlerAuthenticationPolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

/**
 * This is {@link ExcludedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@EqualsAndHashCode
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExcludedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria implements RegisteredServiceAuthenticationPolicyCriteria {
    private static final long serialVersionUID = -7298017804877275864L;

    @Override
    public AuthenticationPolicy toAuthenticationPolicy(final RegisteredService registeredService) {
        val handlers = registeredService.getAuthenticationPolicy().getExcludedAuthenticationHandlers();
        return new ExcludedAuthenticationHandlerAuthenticationPolicy(handlers, false);
    }
}

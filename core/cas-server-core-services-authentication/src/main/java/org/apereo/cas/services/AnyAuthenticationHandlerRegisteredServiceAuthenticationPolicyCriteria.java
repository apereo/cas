package org.apereo.cas.services;

import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@EqualsAndHashCode
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria implements RegisteredServiceAuthenticationPolicyCriteria {
    private static final long serialVersionUID = -2905826778096374574L;

    private boolean tryAll;

    @Override
    public AuthenticationPolicy toAuthenticationPolicy(final RegisteredService registeredService) {
        return new AtLeastOneCredentialValidatedAuthenticationPolicy(this.tryAll);
    }
}

package org.apereo.cas.services;

import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.policy.RestfulAuthenticationPolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * This is {@link RestfulRegisteredServiceAuthenticationPolicyCriteria}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@EqualsAndHashCode
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RestfulRegisteredServiceAuthenticationPolicyCriteria implements RegisteredServiceAuthenticationPolicyCriteria {
    private static final long serialVersionUID = -2915826778096374574L;

    private String url;

    private String basicAuthUsername;

    private String basicAuthPassword;

    @Override
    public AuthenticationPolicy toAuthenticationPolicy(final RegisteredService registeredService) {
        return new RestfulAuthenticationPolicy(this.url, this.basicAuthUsername, this.basicAuthPassword);
    }
}

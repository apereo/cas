package org.apereo.cas.services;

import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.policy.RestfulAuthenticationPolicy;
import org.apereo.cas.configuration.model.core.authentication.RestAuthenticationPolicyProperties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;

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
        val props = new RestAuthenticationPolicyProperties();
        props.setUrl(url);
        props.setBasicAuthUsername(basicAuthUsername);
        props.setBasicAuthPassword(basicAuthPassword);
        return new RestfulAuthenticationPolicy(props);
    }
}

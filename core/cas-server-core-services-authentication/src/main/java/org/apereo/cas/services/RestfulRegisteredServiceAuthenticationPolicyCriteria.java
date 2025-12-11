package org.apereo.cas.services;

import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.policy.RestfulAuthenticationPolicy;
import org.apereo.cas.configuration.model.core.authentication.RestAuthenticationPolicyProperties;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;

import java.io.Serial;

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
    @Serial
    private static final long serialVersionUID = -2915826778096374574L;

    @ExpressionLanguageCapable
    private String url;

    @ExpressionLanguageCapable
    private String basicAuthUsername;

    @ExpressionLanguageCapable
    private String basicAuthPassword;

    @Override
    public AuthenticationPolicy toAuthenticationPolicy(final RegisteredService registeredService) {
        val props = new RestAuthenticationPolicyProperties();
        props.setUrl(SpringExpressionLanguageValueResolver.getInstance().resolve(url));
        props.setBasicAuthUsername(SpringExpressionLanguageValueResolver.getInstance().resolve(basicAuthUsername));
        props.setBasicAuthPassword(SpringExpressionLanguageValueResolver.getInstance().resolve(basicAuthPassword));
        return new RestfulAuthenticationPolicy(props);
    }
}

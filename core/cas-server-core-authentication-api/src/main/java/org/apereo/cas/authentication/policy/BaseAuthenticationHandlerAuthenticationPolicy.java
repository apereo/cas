package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * Authentication security policy that is satisfied iff a specified
 * authentication handler successfully authenticates
 * at least one credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseAuthenticationHandlerAuthenticationPolicy extends BaseAuthenticationPolicy {

    private static final long serialVersionUID = -3871692225877293627L;

    /**
     * Authentication handler name that is required to satisfy policy.
     */
    private Set<String> handlerNames;

    /**
     * Flag to try all credentials before policy is satisfied.
     */
    private boolean tryAll;
    
    protected BaseAuthenticationHandlerAuthenticationPolicy(final String requiredHandlerNames) {
        this(StringUtils.commaDelimitedListToSet(requiredHandlerNames), false);
    }

    @Override
    public AuthenticationPolicyExecutionResult isSatisfiedBy(final Authentication authn,
                                                             final Set<AuthenticationHandler> authenticationHandlers,
                                                             final ConfigurableApplicationContext applicationContext,
                                                             final Optional<Serializable> assertion) {
        var credsOk = true;
        val sum = authn.getSuccesses().size() + authn.getFailures().size();
        if (this.tryAll) {
            credsOk = authn.getCredentials().size() == sum;
        }

        if (!credsOk) {
            LOGGER.warn("Number of provided credentials [{}] does not match the sum of authentication successes and failures [{}]. "
                + "Successful authentication handlers are [{}]", authn.getCredentials().size(), sum, authn.getSuccesses().keySet());
            return AuthenticationPolicyExecutionResult.failure();
        }

        return isSatisfiedByInternal(authn);
    }

    /**
     * Is satisfied by internal checks.
     *
     * @param authn the authn
     * @return the policy execution result
     */
    abstract AuthenticationPolicyExecutionResult isSatisfiedByInternal(Authentication authn);
}

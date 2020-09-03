package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

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
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@AllArgsConstructor
public class RequiredHandlerAuthenticationPolicy extends BaseAuthenticationPolicy {

    private static final long serialVersionUID = -3871692225877293627L;

    /**
     * Authentication handler name that is required to satisfy policy.
     */
    private Set<String> requiredHandlerNames;

    /**
     * Flag to try all credentials before policy is satisfied.
     */
    private boolean tryAll;

    public RequiredHandlerAuthenticationPolicy(final String requiredHandlerNames) {
        this(org.springframework.util.StringUtils.commaDelimitedListToSet(requiredHandlerNames), false);
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authn, final Set<AuthenticationHandler> authenticationHandlers,
                                 final ConfigurableApplicationContext applicationContext) {
        var credsOk = true;
        val sum = authn.getSuccesses().size() + authn.getFailures().size();
        if (this.tryAll) {
            credsOk = authn.getCredentials().size() == sum;
        }

        if (!credsOk) {
            LOGGER.warn("Number of provided credentials [{}] does not match the sum of authentication successes and failures [{}]. "
                + "Successful authentication handlers are [{}]", authn.getCredentials().size(), sum, authn.getSuccesses().keySet());
            return false;
        }

        LOGGER.debug("Examining authentication successes for authentication handler [{}]", this.requiredHandlerNames);
        if (!requiredHandlerNames.isEmpty()) {
            credsOk = authn.getSuccesses()
                .keySet()
                .stream()
                .anyMatch(s -> requiredHandlerNames.contains(s));

            if (!credsOk) {
                LOGGER.warn("Required authentication handler [{}] is not present in the list of recorded successful authentications",
                    this.requiredHandlerNames);
                return false;
            }
        }

        LOGGER.trace("Authentication policy is satisfied");
        return true;
    }
}

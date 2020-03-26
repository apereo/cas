package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

import java.util.Set;

/**
 * Authentication policy that is satisfied by at least one successfully authenticated credential.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor(force = true)
@EqualsAndHashCode
@Setter
@AllArgsConstructor
@Getter
public class AtLeastOneCredentialValidatedAuthenticationPolicy implements AuthenticationPolicy {

    private static final long serialVersionUID = -7484490540437793931L;

    /**
     * Flag to try all credentials before policy is satisfied. Defaults to {@code false}.
     */
    private boolean tryAll;

    private int order = Ordered.LOWEST_PRECEDENCE;

    public AtLeastOneCredentialValidatedAuthenticationPolicy(final boolean tryAll) {
        this(tryAll, Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authn, final Set<AuthenticationHandler> authenticationHandlers,
                                 final ConfigurableApplicationContext applicationContext) throws Exception {
        if (this.tryAll) {
            val sum = authn.getSuccesses().size() + authn.getFailures().size();
            if (authenticationHandlers.size() != sum) {
                LOGGER.warn("Number of credentials [{}] does not match the sum of authentication successes and failures [{}]", authn.getCredentials().size(), sum);
                return false;
            }
            LOGGER.debug("Authentication policy is satisfied with all authentication transactions");
            return !authn.getSuccesses().isEmpty();
        }
        if (!authn.getSuccesses().isEmpty()) {
            LOGGER.debug("Authentication policy is satisfied having found at least one authentication transactions");
            return true;
        }
        LOGGER.warn("Authentication policy has failed to find a successful authentication transaction");
        return false;
    }
}

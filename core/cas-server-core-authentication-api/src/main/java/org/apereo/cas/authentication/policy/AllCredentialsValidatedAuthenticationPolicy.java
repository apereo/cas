package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;

/**
 * Authentication security policy that is satisfied iff all given credentials are successfully authenticated.
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
public class AllCredentialsValidatedAuthenticationPolicy extends BaseAuthenticationPolicy {
    private static final long serialVersionUID = 6112280265093249844L;

    @Override
    public boolean isSatisfiedBy(final Authentication authn,
                                 final Set<AuthenticationHandler> authenticationHandlers,
                                 final ConfigurableApplicationContext applicationContext) {
        LOGGER.debug("Successful authentications: [{}], credentials: [{}]", authn.getSuccesses().keySet(), authn.getCredentials());
        if (authn.getSuccesses().size() != authn.getCredentials().size()) {
            LOGGER.warn("Number of successful authentications, [{}], does not match the number of provided credentials, [{}].",
                authn.getSuccesses().size(), authn.getCredentials().size());
            return false;
        }
        LOGGER.debug("Authentication policy is satisfied.");
        return true;
    }
}

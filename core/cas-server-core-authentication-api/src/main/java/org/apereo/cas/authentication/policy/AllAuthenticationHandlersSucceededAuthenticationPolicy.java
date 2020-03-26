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
import java.util.stream.Collectors;

/**
 * Authentication security policy that is satisfied iff all given authentication handlers are successfully authenticated.
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
public class AllAuthenticationHandlersSucceededAuthenticationPolicy extends BaseAuthenticationPolicy {
    private static final long serialVersionUID = 8901190843828760737L;

    @Override
    public boolean isSatisfiedBy(final Authentication authn, final Set<AuthenticationHandler> authenticationHandlers,
                                 final ConfigurableApplicationContext applicationContext) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Successful authentications: [{}], current authentication handlers [{}]", authn.getSuccesses().keySet(),
                authenticationHandlers.stream().map(AuthenticationHandler::getName).collect(Collectors.joining(",")));
        }

        if (authn.getSuccesses().size() != authenticationHandlers.size()) {
            LOGGER.warn("Number of successful authentications, [{}], does not match the number of authentication handlers, [{}].",
                authn.getSuccesses().size(), authenticationHandlers.size());
            return false;
        }
        return !authn.getSuccesses().isEmpty();
    }
}

package org.jasig.cas.authentication;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Authentication security policy that is satisfied iff all given credentials are successfully authenticated.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RefreshScope
@Component("allAuthenticationPolicy")
public class AllAuthenticationPolicy implements AuthenticationPolicy {

    @Override
    public boolean isSatisfiedBy(final Authentication authn) {
        return authn.getSuccesses().size() == authn.getCredentials().size();
    }
}

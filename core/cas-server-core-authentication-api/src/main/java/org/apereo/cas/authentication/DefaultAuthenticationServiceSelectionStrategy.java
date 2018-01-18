package org.apereo.cas.authentication;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Service;
import org.springframework.core.Ordered;
import lombok.Setter;

/**
 * This is {@link DefaultAuthenticationServiceSelectionStrategy} which returns back to the caller
 * the provided service, as it was without any additional processing. 
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Setter
@Getter
public class DefaultAuthenticationServiceSelectionStrategy implements AuthenticationServiceSelectionStrategy {

    private static final long serialVersionUID = -7458940344679793681L;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Service resolveServiceFrom(final Service service) {
        return service;
    }

    @Override
    public boolean supports(final Service service) {
        return true;
    }

}

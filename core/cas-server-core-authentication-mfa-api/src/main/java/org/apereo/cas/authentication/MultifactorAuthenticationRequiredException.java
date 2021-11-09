package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link MultifactorAuthenticationRequiredException}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@NoArgsConstructor(force = true)
@Getter
@RequiredArgsConstructor
public class MultifactorAuthenticationRequiredException extends AuthenticationException {
    private static final long serialVersionUID = 5909155188558680032L;

    private static final String CODE = "MULTIFACTOR_AUTHN_REQUIRED";
    
    private final RegisteredService service;

    private final Principal principal;

    @Override
    public String getCode() {
        return CODE;
    }
}

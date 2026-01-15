package org.apereo.cas.mfa.simple.validation;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;

/**
 * This is {@link CasSimpleMultifactorAuthenticationAccountService}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
public interface CasSimpleMultifactorAuthenticationAccountService {

    /**
     * Bean name.
     */
    String BEAN_NAME = "casSimpleMultifactorAuthenticationAccountService";

    /**
     * Update.
     *
     * @param principal  the principal
     * @param attributes the attributes
     */
    void update(Principal principal, Map<String, Object> attributes);
}

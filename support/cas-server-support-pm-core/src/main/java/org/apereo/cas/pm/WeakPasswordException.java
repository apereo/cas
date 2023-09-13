package org.apereo.cas.pm;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.flow.resolver.CasWebflowAware;
import java.io.Serial;

/**
 * Raised when password management detects a weak password that does not match against
 * the CAS password policy rules, or via other dynamic conditions.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class WeakPasswordException extends InvalidPasswordException implements CasWebflowAware {
    @Serial
    private static final long serialVersionUID = 458954862481279L;

    public WeakPasswordException(final UsernamePasswordCredential credential) {
        super("Detected weak password for user %s".formatted(credential.getId()));
    }
}

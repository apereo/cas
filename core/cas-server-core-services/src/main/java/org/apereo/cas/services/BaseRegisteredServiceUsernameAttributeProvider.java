package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;

import java.util.Locale;

/**
 * This is {@link BaseRegisteredServiceUsernameAttributeProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseRegisteredServiceUsernameAttributeProvider implements RegisteredServiceUsernameAttributeProvider {
    private static final long serialVersionUID = -8381275200333399951L;

    private String canonicalizationMode = CaseCanonicalizationMode.NONE.name();

    public BaseRegisteredServiceUsernameAttributeProvider() {
        setCanonicalizationMode(CaseCanonicalizationMode.NONE.name());
    }

    public BaseRegisteredServiceUsernameAttributeProvider(final String canonicalizationMode) {
        this.canonicalizationMode = canonicalizationMode;
    }

    @Override
    public final String resolveUsername(final Principal principal, final Service service) {
        final String username = resolveUsernameInternal(principal, service);
        return CaseCanonicalizationMode.valueOf(canonicalizationMode).canonicalize(username.trim(), Locale.getDefault());
    }

    /**
     * Resolve username internal string.
     *
     * @param principal the principal
     * @param service   the service
     * @return the string
     */
    protected abstract String resolveUsernameInternal(Principal principal, Service service);

    public String getCanonicalizationMode() {
        return canonicalizationMode;
    }

    public void setCanonicalizationMode(final String canonicalizationMode) {
        this.canonicalizationMode = canonicalizationMode;
    }
}

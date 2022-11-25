package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;

import jakarta.persistence.PostLoad;

import java.io.Serial;
import java.util.Locale;
import java.util.Optional;

/**
 * This is {@link BaseRegisteredServiceUsernameAttributeProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseRegisteredServiceUsernameAttributeProvider implements RegisteredServiceUsernameAttributeProvider {

    @Serial
    private static final long serialVersionUID = -8381275200333399951L;

    private String canonicalizationMode = CaseCanonicalizationMode.NONE.name();

    private boolean encryptUsername;

    private String scope;

    private String removePattern;

    @Override
    public final String resolveUsername(final Principal principal, final Service service, final RegisteredService registeredService) {
        val resolvedUsername = resolveUsernameInternal(principal, service, registeredService);
        if (canonicalizationMode == null) {
            canonicalizationMode = CaseCanonicalizationMode.NONE.name();
        }
        val removedUsername = removePatternFromUsernameIfNecessary(resolvedUsername);
        val finalUsername = scopeUsernameIfNecessary(removedUsername);
        val uid = CaseCanonicalizationMode.valueOf(canonicalizationMode).canonicalize(finalUsername.trim(), Locale.getDefault());
        LOGGER.debug("Resolved username for [{}] is [{}]", service, uid);
        if (!this.encryptUsername) {
            return uid;
        }
        val encryptedId = encryptResolvedUsername(principal, service, registeredService, uid);
        if (StringUtils.isBlank(encryptedId)) {
            throw new IllegalArgumentException("Could not encrypt username " + uid + " for service " + service);
        }
        return encryptedId;
    }

    /**
     * Initializes the registered service with default values
     * for fields that are unspecified. Only triggered by JPA.
     */
    @PostLoad
    public void initialize() {
        setCanonicalizationMode(CaseCanonicalizationMode.NONE.name());
    }

    protected String removePatternFromUsernameIfNecessary(final String username) {
        return FunctionUtils.doIfNotNull(removePattern, () -> RegExUtils.removePattern(username, removePattern), () -> username).get();
    }

    protected String scopeUsernameIfNecessary(final String resolved) {
        return FunctionUtils.doIfNotNull(scope, () -> String.format("%s@%s", resolved, scope), () -> resolved).get();
    }

    /**
     * Encrypt resolved username.
     *
     * @param principal         the principal
     * @param service           the service
     * @param registeredService the registered service
     * @param username          the username
     * @return the encrypted username or null
     */
    protected String encryptResolvedUsername(final Principal principal, final Service service, final RegisteredService registeredService, final String username) {
        val applicationContext = ApplicationContextProvider.getApplicationContext();
        val cipher = applicationContext.getBean(RegisteredServiceCipherExecutor.DEFAULT_BEAN_NAME, RegisteredServiceCipherExecutor.class);
        return cipher.encode(username, Optional.of(registeredService));
    }

    /**
     * Resolve username internal string.
     *
     * @param principal         the principal
     * @param service           the service
     * @param registeredService the registered service
     * @return the string
     */
    protected abstract String resolveUsernameInternal(Principal principal, Service service, RegisteredService registeredService);

}

package org.apereo.cas.services;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.services.persondir.util.CaseCanonicalizationMode;
import org.springframework.context.ApplicationContext;
import javax.persistence.PostLoad;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;

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
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseRegisteredServiceUsernameAttributeProvider implements RegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = -8381275200333399951L;

    private String canonicalizationMode = CaseCanonicalizationMode.NONE.name();

    private boolean encryptUsername;

    @Override
    public final String resolveUsername(final Principal principal, final Service service, final RegisteredService registeredService) {
        final String username = resolveUsernameInternal(principal, service, registeredService);
        if (canonicalizationMode == null) {
            canonicalizationMode = CaseCanonicalizationMode.NONE.name();
        }
        final String uid = CaseCanonicalizationMode.valueOf(canonicalizationMode).canonicalize(username.trim(), Locale.getDefault());
        LOGGER.debug("Resolved username for [{}] is [{}]", service.getId(), uid);
        if (!this.encryptUsername) {
            return uid;
        }
        final String encryptedId = encryptResolvedUsername(principal, service, registeredService, uid);
        if (StringUtils.isBlank(encryptedId)) {
            throw new IllegalArgumentException("Could not encrypt username " + uid + " for service " + service);
        }
        return encryptedId;
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
        final ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        final RegisteredServiceCipherExecutor cipher = applicationContext.getBean("registeredServiceCipherExecutor", RegisteredServiceCipherExecutor.class);
        return cipher.encode(username, registeredService);
    }

    /**
     * Initializes the registered service with default values
     * for fields that are unspecified. Only triggered by JPA.
     */
    @PostLoad
    public void initialize() {
        setCanonicalizationMode(CaseCanonicalizationMode.NONE.name());
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

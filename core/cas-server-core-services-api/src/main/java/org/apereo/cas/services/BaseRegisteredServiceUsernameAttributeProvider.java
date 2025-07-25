package org.apereo.cas.services;

import org.apereo.cas.authentication.attribute.CaseCanonicalizationMode;
import org.apereo.cas.util.function.FunctionUtils;
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

    private String canonicalizationMode = "NONE";

    private boolean encryptUsername;

    private String scope;

    private String removePattern;

    @Override
    public final String resolveUsername(final RegisteredServiceUsernameProviderContext context) throws Throwable {
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(context.getService(), context.getRegisteredService());
            
        val resolvedUsername = resolveUsernameInternal(context);
        if (canonicalizationMode == null) {
            canonicalizationMode = "NONE";
        }
        val removedUsername = removePatternFromUsernameIfNecessary(resolvedUsername);
        val finalUsername = scopeUsernameIfNecessary(removedUsername);
        val uid = CaseCanonicalizationMode.valueOf(canonicalizationMode).canonicalize(finalUsername.trim(), Locale.getDefault());
        LOGGER.debug("Resolved username for [{}] is [{}]", context.getService(), uid);
        if (!this.encryptUsername) {
            return uid;
        }
        val encryptedId = encryptResolvedUsername(context, uid);
        if (StringUtils.isBlank(encryptedId)) {
            throw new IllegalArgumentException("Could not encrypt username " + uid + " for service " + context.getService());
        }
        return encryptedId;
    }

    /**
     * Initializes the registered service with default values
     * for fields that are unspecified. Only triggered by JPA.
     */
    @PostLoad
    public void initialize() {
        setCanonicalizationMode("NONE");
    }

    protected String removePatternFromUsernameIfNecessary(final String username) {
        return FunctionUtils.doIfNotNull(removePattern, () -> RegExUtils.removePattern((CharSequence) username, removePattern), () -> username).get();
    }

    protected String scopeUsernameIfNecessary(final String resolved) {
        return FunctionUtils.doIfNotNull(scope, () -> String.format("%s@%s", resolved, scope), () -> resolved).get();
    }

    protected String encryptResolvedUsername(final RegisteredServiceUsernameProviderContext context, final String username) {
        val cipher = context.getApplicationContext().getBean(RegisteredServiceCipherExecutor.DEFAULT_BEAN_NAME, RegisteredServiceCipherExecutor.class);
        return cipher.encode(username, Optional.of(context.getRegisteredService()));
    }

    protected abstract String resolveUsernameInternal(RegisteredServiceUsernameProviderContext context) throws Throwable;

}

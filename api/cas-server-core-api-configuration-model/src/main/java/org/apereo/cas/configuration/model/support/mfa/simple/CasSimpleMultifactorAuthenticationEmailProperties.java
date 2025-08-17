package org.apereo.cas.configuration.model.support.mfa.simple;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.util.regex.Pattern;

/**
 * This is {@link CasSimpleMultifactorAuthenticationEmailProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-simple-mfa")
@Getter
@Setter
@Accessors(chain = true)
public class CasSimpleMultifactorAuthenticationEmailProperties extends EmailProperties implements CasFeatureModule {

    @Serial
    private static final long serialVersionUID = -4089345892508037667L;

    /**
     * The regular expression that controls which email addresses users are allowed to register
     * during the multifactor authentication flow. If no pattern is defined (default),
     * registration flow will be disabled. An example email address might
     * be {@code ^[a-zA-Z0-9._%+-]+@example\.org$}.
     */
    @RegularExpressionCapable
    private Pattern acceptedEmailPattern;

    @JsonIgnore
    public boolean isRegistrationEnabled() {
        return acceptedEmailPattern != null;
    }
}

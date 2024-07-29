package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link PasswordManagementCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pm-webflow")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)

public class PasswordManagementCoreProperties implements CasFeatureModule, Serializable {

    @Serial
    private static final long serialVersionUID = -261644582798411176L;

    /**
     * Flag to indicate if password management facility is enabled.
     */
    @RequiredProperty
    private boolean enabled;

    /**
     * Flag to indicate whether successful password change should trigger login automatically.
     */
    private boolean autoLogin;

    /**
     * A String value representing password policy regex pattern.
     * Minimum 8 and Maximum 10 characters at least 1 Uppercase
     * Alphabet, 1 Lowercase Alphabet, 1 Number and 1 Special Character.
     */
    @RequiredProperty
    @RegularExpressionCapable
    private String passwordPolicyPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,10}";

    /**
     * The character set that CAS may use to generate and suggest new passwords.
     */
    private String passwordPolicyCharacterSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789![]{}()%&*$#^<>~@|";

    /**
     * The password length used by CAS when suggesting generated passwords.
     */
    private long passwordPolicyPasswordLength = 10;
}

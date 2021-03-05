package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.util.Map;

/**
 * Configuration properties class for password.policy.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("PasswordPolicyProperties")
public class PasswordPolicyProperties implements Serializable {
    private static final long serialVersionUID = -3878237508646993100L;

    /**
     * Decide how authentication should handle password policy changes.
     */
    private PasswordPolicyHandlingOptions strategy = PasswordPolicyHandlingOptions.DEFAULT;

    /**
     * Key-value structure (Map) that indicates a list of boolean attributes as keys.
     * If either attribute value is true, indicating an account state is flagged,
     * the corresponding error can be thrown.
     * Example {@code accountLocked=javax.security.auth.login.AccountLockedException}
     */
    private Map<String, Class<? extends LoginException>> policyAttributes = new LinkedCaseInsensitiveMap<>();

    /**
     * Whether password policy should be enabled.
     */
    private boolean enabled = true;

    /**
     * Indicates whether account state handling should be enabled to process
     * warnings or errors reported back from the authentication response, produced by the source.
     */
    private boolean accountStateHandlingEnabled = true;

    /**
     * When dealing with FreeIPA, indicates the number of allows login failures.
     */
    private int loginFailures = 5;

    /**
     * Used by an account state handling policy that only calculates account warnings
     * in case the entry carries an attribute {@link #warningAttributeName}
     * whose value matches this field.
     */
    private String warningAttributeValue;

    /**
     * Used by an account state handling policy that only calculates account warnings
     * in case the entry carries this attribute.
     */
    private String warningAttributeName;

    /**
     * Indicates if warning should be displayed, when the ldap attribute value
     * matches the {@link #warningAttributeValue}.
     */
    private boolean displayWarningOnMatch = true;

    /**
     * Always display the password expiration warning regardless.
     */
    private boolean warnAll;

    /**
     * This is used to calculate
     * a warning period to see if account expiry is within the calculated window.
     */
    private int warningDays = 30;

    /**
     * Handle password policy via Groovy script.
     */
    @NestedConfigurationProperty
    private GroovyPasswordPolicyProperties groovy = new GroovyPasswordPolicyProperties();

    /**
     * The Password policy handling options.
     */
    public enum PasswordPolicyHandlingOptions {
        /**
         * Default option to handle policy changes.
         */
        DEFAULT,
        /**
         * Handle account password policies via Groovy.
         */
        GROOVY,
        /**
         * Strategy to only activate password policy
         * if the authentication response code is not blocked.
         */
        REJECT_RESULT_CODE
    }

}

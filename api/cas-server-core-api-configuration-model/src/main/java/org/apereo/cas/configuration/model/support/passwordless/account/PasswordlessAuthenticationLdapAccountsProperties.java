package org.apereo.cas.configuration.model.support.passwordless.account;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link PasswordlessAuthenticationLdapAccountsProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-passwordless-ldap")
@Getter
@Setter
@Accessors(chain = true)
public class PasswordlessAuthenticationLdapAccountsProperties extends AbstractLdapSearchProperties {
    @Serial
    private static final long serialVersionUID = -1102345678378393382L;

    /**
     * Name of the LDAP attribute that
     * indicates the user's email address.
     */
    @RequiredProperty
    private String emailAttribute = "mail";

    /**
     * Name of the LDAP attribute that
     * indicates the user's phone.
     */
    @RequiredProperty
    private String phoneAttribute = "phoneNumber";

    /**
     * Name of the LDAP attribute that
     * indicates the user's name.
     *
     * @deprecated This property will likely be removed in v8.
     */
    @Deprecated
    private String nameAttribute = "cn";

    /**
     * Name of the LDAP attribute that
     * indicates the username.
     */
    private String usernameAttribute;

    /**
     * Name of the LDAP attribute that
     * is the passwordless flow to request a password prompt from user.
     * The attribute value must be a boolean. Accepted values
     * are {@code true}, {@code false}, {@code on}, {@code off}, {@code yes}, {@code no},
     * {@code Y}, {@code T}, {@code F}, {@code N}, etc.
     * Comparisons are not case sensitive.
     */
    private String requestPasswordAttribute = "requestPassword";

    /**
     * Name of the LDAP attribute that is required to be present
     * in the user account entry for passwordless authentication to trigger.
     * Presence of this attribute is checked to ensure the user account
     * is allowed to use passwordless authentication.
     */
    private String requiredAttribute;

    /**
     * The required attribute value that must be present in the user account entry
     * for passwordless authentication to trigger. Presence of this attribute is checked
     * to ensure the user account is allowed to use passwordless authentication.
     * The value can be a regular expression pattern.
     */
    @RegularExpressionCapable
    private String requiredAttributeValue;
}

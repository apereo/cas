package org.apereo.cas.configuration.model.support.passwordless.account;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("PasswordlessAuthenticationLdapAccountsProperties")
public class PasswordlessAuthenticationLdapAccountsProperties extends AbstractLdapSearchProperties {
    @Serial
    private static final long serialVersionUID = -1102345678378393382L;

    /**
     * Name of the LDAP attribute that
     * indicates the user's email address.
     */
    private String emailAttribute = "mail";

    /**
     * Name of the LDAP attribute that
     * indicates the user's phone.
     */
    private String phoneAttribute = "phoneNumber";

    /**
     * Name of the LDAP attribute that
     * indicates the user's name.
     */
    private String nameAttribute = "cn";

    /**
     * Name of the LDAP attribute that
     * is the passwordless flow to request a password prompt from user.
     * The attribute value must be a boolean. Acceoted values
     * are {@code true}, {@code false}, {@code on}, {@code off}, {@code yes}, {@code no},
     * {@code Y}, {@code T}, {@code F}, {@code N}, etc.
     * Comparisons are not case sensitive.
     */
    private String requestPasswordAttribute = "requestPassword";
}

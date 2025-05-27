package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link LdapPasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-pm-ldap")
@Getter
@Setter

@Accessors(chain = true)
public class LdapPasswordManagementProperties extends AbstractLdapSearchProperties {
    @Serial
    private static final long serialVersionUID = -2610186056194686825L;

    /**
     * Collection of attribute names that indicate security questions answers.
     * This is done via a key-value structure where the key is the attribute name
     * for the security question and the value is the attribute name for the answer linked to the question.
     */
    private Map<String, String> securityQuestionsAttributes = new LinkedHashMap<>();

    /**
     * Name of LDAP attribute that represents the account locked status.
     * The value of the attribute is typically set to {@code "true"} if the account is
     * ever updated to indicated a locked status. For Active Directory, this attribute
     * might be called {@code lockoutTime}.
     */
    @RequiredProperty
    private String accountLockedAttribute = "pwdLockout";

    /**
     * When CAS is about to unlock the user account,
     * it will use the {@link #accountLockedAttribute} setting to
     * locate the appropriate attribute for the user entry. This attribute
     * will then be assigned the value(s) defined here to unlock the account.
     * <p>For Active Directory and in scenarios where {@link #accountLockedAttribute} is set to
     * {@code lockoutTime}, this value might be set to zero. A value of zero means
     * that the account is not currently locked out.
     * <p>Note that the value defined here may be treated as case sensitive by the LDAP server.
     */
    private String[] accountUnlockedAttributeValues = {"FALSE"};

    /**
     * The specific variant of LDAP
     * based on which update operations will be constructed.
     * Accepted values are:
     * * <ul>
     * <li>{@code AD}</li>
     * <li>{@code GENERIC}</li>
     * <li>{@code FreeIPA}</li>
     * <li>{@code EDirectory}</li>
     * </ul>
     */
    @RequiredProperty
    private LdapType type = LdapType.AD;

    /**
     * Username attribute required by LDAP.
     */
    @RequiredProperty
    private String usernameAttribute = "uid";
}

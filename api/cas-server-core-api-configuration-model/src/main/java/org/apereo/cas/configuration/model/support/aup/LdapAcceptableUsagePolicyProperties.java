package org.apereo.cas.configuration.model.support.aup;

import module java.base;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link LdapAcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-aup-ldap")
@Getter
@Setter
@Accessors(chain = true)
public class LdapAcceptableUsagePolicyProperties extends AbstractLdapSearchProperties {
    @Serial
    private static final long serialVersionUID = -7991011278378393382L;

    /**
     * Attribute value that indicates whether AUP has been accepted
     * for the LDAP record.
     */
    private String aupAcceptedAttributeValue = Boolean.TRUE.toString().toUpperCase(Locale.ENGLISH);
}

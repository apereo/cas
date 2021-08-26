package org.apereo.cas.configuration.model.support.x509;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link X509LdapProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-x509-webflow")
public class X509LdapProperties extends AbstractLdapSearchProperties {

    private static final long serialVersionUID = -1655068554291000206L;

    /**
     * The LDAP attribute that holds the certificate revocation list.
     */
    private String certificateAttribute = "certificateRevocationList";

}

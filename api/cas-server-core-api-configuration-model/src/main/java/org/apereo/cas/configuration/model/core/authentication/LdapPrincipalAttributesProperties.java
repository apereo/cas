package org.apereo.cas.configuration.model.core.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link LdapPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-person-directory", automated = true)
@Slf4j
@Getter
@Setter
public class LdapPrincipalAttributesProperties extends AbstractLdapSearchProperties {

    private static final long serialVersionUID = 5760065368731012063L;

    /**
     * The order of this attribute repository in the chain of repositories.
     * Can be used to explicitly position this source in chain and affects
     * merging strategies.
     */
    private int order;

    /**
     * Map of attributes to fetch from the source.
     * Attributes are defined using a key-value structure
     * where CAS allows the attribute name/key to be renamed virtually
     * to a different attribute. The key is the attribute fetched
     * from the data source and the value is the attribute name CAS should
     * use for virtual renames.
     */
    private Map<String, String> attributes = new HashMap();
}

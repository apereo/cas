package org.apereo.cas.configuration.model.support.gua;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link GraphicalUserAuthenticationProperties}
 * that contains settings needed for identification
 * of users graphically prior to executing primary authn.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-gua")
@Getter
@Setter
@Accessors(chain = true)
public class GraphicalUserAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 7527953699378415460L;

    /**
     * Locate GUA settings and images from LDAP.
     */
    @NestedConfigurationProperty
    private LdapGraphicalUserAuthenticationProperties ldap = new LdapGraphicalUserAuthenticationProperties();

    /**
     * Locate GUA settings and images from a static image per user.
     * This is treated as a {@link Map} where the key is the user id
     * and the value should be the graphical resource.
     */
    private Map<String, String> simple = new LinkedHashMap<>();
}

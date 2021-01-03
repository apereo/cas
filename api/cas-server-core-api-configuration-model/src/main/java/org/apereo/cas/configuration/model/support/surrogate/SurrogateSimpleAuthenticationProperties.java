package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link SurrogateSimpleAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-surrogate-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SurrogateSimpleAuthenticationProperties")
public class SurrogateSimpleAuthenticationProperties extends AbstractLdapSearchProperties {
    private static final long serialVersionUID = 16938920863432222L;

    /**
     * Define the list of accounts that are allowed to impersonate.
     * This is done in a key-value structure where the key is the admin user
     * and the value is a comma-separated list of identifiers that can be
     * impersonated by the admin-user.
     */
    private Map<String, String> surrogates = new LinkedHashMap<>(2);

}

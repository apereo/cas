package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link LdapPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-person-directory", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("LdapPrincipalAttributesProperties")
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
     * 
     * Attributes may be allowed to be virtually renamed and remapped. The key in the
     * attribute map is the original attribute,
     * and the value should be the virtually-renamed attribute.
     *
     * To fetch and resolve attributes that carry tags/options,
     * consider tagging the mapped attribute as such: {@code affiliation=affiliation}.
     */
    private Map<String, String> attributes = new LinkedHashMap<>();

    /**
     * A value can be assigned to this field to uniquely identify this resolver.
     */
    private String id;
}

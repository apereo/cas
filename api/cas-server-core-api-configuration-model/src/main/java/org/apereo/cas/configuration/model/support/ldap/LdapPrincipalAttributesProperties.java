package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.util.HashMap;
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
public class LdapPrincipalAttributesProperties extends AbstractLdapSearchProperties {

    @Serial
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
     * <p>
     * Attributes may be allowed to be virtually renamed and remapped. The key in the
     * attribute map is the original attribute,
     * and the value should be the virtually-renamed attribute.
     * <p>
     * To fetch and resolve attributes that carry tags/options, such as {@code homePostalAddress;lang-jp}
     * consider tagging the mapped attribute as such: {@code affiliation=affiliation;}.
     * <p>
     * Values may be separated by a comma, which allows you to rename the source attribute
     * into multiple CAS attributes. For example {@code mail=mail,email} would fetch the {@code mail}
     * attribute from the data source and virtually rename it into two CAS attributes: {@code mail} and {@code email}.
     */
    private Map<String, String> attributes = new LinkedHashMap<>();

    /**
     * A value can be assigned to this field to uniquely identify this resolver.
     */
    private String id;

    /**
     * Whether attribute resolution based on this source is enabled.
     */
    private AttributeRepositoryStates state = AttributeRepositoryStates.ACTIVE;

    /**
     * Whether all existing attributes should be passed
     * down to the query builder map and be used in the construction
     * of the filter.
     */
    private boolean useAllQueryAttributes = true;

    /**
     * Define a {@code Map} of query attribute names to data-layer attribute names to use when building the query.
     * The key is always the name of the query attribute that is defined by CAS and passed internally,
     * and the value is the column/field that should map.
     */
    private Map<String, String> queryAttributes = new HashMap<>();


}

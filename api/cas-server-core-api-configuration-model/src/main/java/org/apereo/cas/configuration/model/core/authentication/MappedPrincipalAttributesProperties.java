package org.apereo.cas.configuration.model.core.authentication;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.Ordered;

/**
 * This is {@link MappedPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-person-directory", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class MappedPrincipalAttributesProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 7017508256487553063L;

    /**
     * Map of people and their attributes. The structure follows a key-value pair where key is the principal id
     * and value is a <i>directed list</i> of attributes associated with that principal.
     * Example: {@code casuser=eduPersonAffiliation->faculty,name->casuser}
     */
    private Map<String, List<String>> people = new HashMap<>();

    /**
     * A value can be assigned to this field to uniquely identify this resolver.
     */
    private String id;

    /**
     * The order of this attribute repository in the chain of repositories.
     * Can be used to explicitly position this source in chain and affects
     * merging strategies.
     */
    private int order = Ordered.LOWEST_PRECEDENCE;

    /**
     * Whether attribute resolution based on this source is enabled.
     */
    private AttributeRepositoryStates state = AttributeRepositoryStates.ACTIVE;
}

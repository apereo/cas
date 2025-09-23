package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.Ordered;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link StubPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-person-directory", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class StubPrincipalAttributesProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 7017508256487553063L;

    /**
     * Static attributes that need to be mapped to a hardcoded value belong here.
     * The structure follows a key-value pair where key is the attribute name
     * and value is the attribute value. The key is the attribute fetched
     * from the source and the value is the attribute name CAS should
     * use for virtual renames.
     * <p>
     * Attributes may be allowed to be virtually renamed and remapped. The key in the
     * attribute map is the original attribute,
     * and the value should be the virtually-renamed attribute.
     */
    private Map<String, String> attributes = new HashMap<>();

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

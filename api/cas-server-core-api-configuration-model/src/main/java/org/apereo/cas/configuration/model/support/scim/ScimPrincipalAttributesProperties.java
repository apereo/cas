package org.apereo.cas.configuration.model.support.scim;

import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link ScimPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-support-scim")
@Getter
@Setter
@Accessors(chain = true)
public class ScimPrincipalAttributesProperties extends ScimProperties {
    @Serial
    private static final long serialVersionUID = 1803921759386037000L;

    /**
     * The order of this attribute repository in the chain of repositories.
     * Can be used to explicitly position this source in chain and affects
     * merging strategies.
     */
    private int order;

    /**
     * A value can be assigned to this field to uniquely identify this resolver.
     */
    private String id;

    /**
     * Whether attribute resolution based on this source is enabled.
     */
    private AttributeRepositoryStates state = AttributeRepositoryStates.ACTIVE;
}

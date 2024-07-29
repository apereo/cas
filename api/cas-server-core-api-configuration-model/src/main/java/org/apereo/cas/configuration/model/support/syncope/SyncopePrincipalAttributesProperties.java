package org.apereo.cas.configuration.model.support.syncope;

import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link SyncopePrincipalAttributesProperties}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-syncope-authentication")
@Getter
@Setter
@Accessors(chain = true)

public class SyncopePrincipalAttributesProperties extends BaseSyncopeSearchProperties {

    @Serial
    private static final long serialVersionUID = 98257222402164L;

    /**
     * A value can be assigned to this field to uniquely identify this resolver.
     */
    private String id;

    /**
     * The order of this attribute repository in the chain of repositories.
     * Can be used to explicitly position this source in chain and affects
     * merging strategies.
     */
    private int order;

    /**
     * Whether attribute resolution based on this source is enabled.
     */
    private AttributeRepositoryStates state = AttributeRepositoryStates.ACTIVE;
}

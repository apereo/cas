package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GroovyPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-person-directory", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class GroovyPrincipalAttributesProperties extends SpringResourceProperties {

    private static final long serialVersionUID = 7901595963842506684L;

    /**
     * Whether attribute repository should consider the underlying
     * attribute names in a case-insensitive manner.
     */
    private boolean caseInsensitive;

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
}

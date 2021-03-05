package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * If you wish to directly and separately retrieve attributes from a static JSON source.
 * The resource syntax must be as such:
 * &lt;pre&gt;
 * {
 * "user1": {
 * "firstName":["Json1"],
 * "lastName":["One"]
 * },
 * "user2": {
 * "firstName":["Json2"],
 * "eduPersonAffiliation":["employee", "student"]
 * }
 * }
 * &lt;/pre&gt;
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-person-directory", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class JsonPrincipalAttributesProperties extends SpringResourceProperties {

    private static final long serialVersionUID = -6573755681498251678L;

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

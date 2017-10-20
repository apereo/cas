package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

/**
 * This is {@link JsonPrincipalAttributesProperties}.
 * <p>
 * If you wish to directly and separately retrieve attributes from a static JSON source.
 * The resource syntax must be as such:
 * <pre>
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
 * </pre>
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-person-directory", automated = true)
public class JsonPrincipalAttributesProperties extends SpringResourceProperties {
    private static final long serialVersionUID = -6573755681498251678L;
    /**
     * The order of this attribute repository in the chain of repositories.
     * Can be used to explicitly position this source in chain and affects
     * merging strategies.
     */
    private int order;

    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }
}


package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link RestPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-person-directory", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("RestPrincipalAttributesProperties")
public class RestPrincipalAttributesProperties extends RestEndpointProperties {

    @Serial
    private static final long serialVersionUID = -30055974448426360L;

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
     * The attribute name that would be used to look up and
     * determine the user id from the query map. The value
     * linked to this attribute would be used as the username
     * or subject by the attribute repository to pass on
     * to the ultimate source to locate the user record.
     */
    private String usernameAttribute = "username";

    /**
     * Whether attribute resolution based on this source is enabled.
     */
    private AttributeRepositoryStates state = AttributeRepositoryStates.ACTIVE;
}

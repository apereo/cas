package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link GrouperPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-person-directory", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("GrouperPrincipalAttributesProperties")
public class GrouperPrincipalAttributesProperties implements Serializable {

    private static final long serialVersionUID = 7139471665871712818L;

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

    /**
     * A value can be assigned to this field to uniquely identify this resolver.
     */
    private String id;

    /**
     * Indicate how the username passed to the attribute repository
     * should be set and treated by the grouper client to look up records.
     *
     * Accepted values are: {@code SUBJECT_IDENTIFIER}, {@code SUBJECT_ATTRIBUTE_NAME}, {@code SUBJECT_ID}.
     */
    private String subjectType = "SUBJECT_ID";
    
    /**
     * The attribute name that would be used to look up and
     * determine the user id from the query map. The value
     * linked to this attribute would be used as the username
     * or subject by the attribute repository to pass on
     * to the ultimate source to locate the user record.
     */
    private String usernameAttribute = "username";

    /**
     * Custom parameters defined as a {@link Map} to pass onto the attribute repository
     * which ultimately will be passed onto the grouper client.
     * Key is the parameter name and value is the parameter value.
     */
    private Map<String, String> parameters = new HashMap<>();
}

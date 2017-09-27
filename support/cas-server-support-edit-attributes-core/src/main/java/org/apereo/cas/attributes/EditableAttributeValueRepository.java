package org.apereo.cas.attributes;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link EditableAttributeValueRepository}.
 *
 * @author Marcus Watkins
 * @since 5.2
 */
public interface EditableAttributeValueRepository extends Serializable {
    /**
     * Gather stored attribute values associated with user.
     *
     * @param requestContext the request context
     * @param credential     the credential
     * @param attributeIds   list of attribute ids to fetch
     * @return list of attributes and the associated principal.
     */
	Pair<Principal, Map<String,String>> getAttributeValues(RequestContext requestContext, Credential credential, Set<String> attributeIds);

    /**
     * Record attribute values.
     *
     * @param requestContext the request context
     * @param credential     the credential
     * @param attributeValues map of key value pairs of attributes to store
     * @return true if choice was saved.
     */
    boolean storeAttributeValues(final RequestContext requestContext, final Credential credential, Map<String,String> attributeValues);

    

    
}

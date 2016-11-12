package org.apereo.cas.support.oauth.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apereo.cas.authentication.principal.AbstractWebApplicationService;
import org.apereo.cas.services.RegisteredService;

/**
 * OAuth web application service.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthWebApplicationService extends AbstractWebApplicationService {

    private static final long serialVersionUID = -4851305887430952052L;

    /**
     * Instantiates a new OAuth web application service impl.
     *
     * @param registeredService the registered service
     */
    public OAuthWebApplicationService(final RegisteredService registeredService) {
        this(registeredService != null ? String.valueOf(registeredService.getId()) : null);
    }

    @JsonCreator
    protected OAuthWebApplicationService(@JsonProperty("id") final String id) {
        super(id, null, null, null);
    }
}

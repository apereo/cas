package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiredModule;

import java.io.Serializable;

/**
 * This is {@link RestAuthenticationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredModule(name = "core-authentication", automated = true)
public class RestAuthenticationPolicyProperties implements Serializable {
    private static final long serialVersionUID = -8979188862774758908L;
    /**
     * Rest endpoint url to contact.
     */
    private String endpoint;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }
}

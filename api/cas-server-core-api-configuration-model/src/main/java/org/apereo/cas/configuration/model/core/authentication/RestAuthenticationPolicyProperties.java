package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link RestAuthenticationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
public class RestAuthenticationPolicyProperties implements Serializable {

    private static final long serialVersionUID = -8979188862774758908L;

    /**
     * Rest endpoint url to contact.
     */
    private String endpoint;
}

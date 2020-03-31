package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.BaseRestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestAuthenticationPolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class RestAuthenticationPolicyProperties extends BaseRestEndpointProperties {
    private static final long serialVersionUID = -8979188862774758908L;
}

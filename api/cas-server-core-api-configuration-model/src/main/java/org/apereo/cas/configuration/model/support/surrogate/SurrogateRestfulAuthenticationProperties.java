package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SurrogateRestfulAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-surrogate-authentication-rest")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SurrogateRestfulAuthenticationProperties")
public class SurrogateRestfulAuthenticationProperties extends RestEndpointProperties {
    private static final long serialVersionUID = 8152273816132989085L;

}

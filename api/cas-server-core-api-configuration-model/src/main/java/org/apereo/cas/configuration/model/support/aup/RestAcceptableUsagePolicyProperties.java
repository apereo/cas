package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestAcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-aup-rest")
@Getter
@Setter
@Accessors(chain = true)
public class RestAcceptableUsagePolicyProperties extends RestEndpointProperties {
    private static final long serialVersionUID = -8102345678378393382L;
}

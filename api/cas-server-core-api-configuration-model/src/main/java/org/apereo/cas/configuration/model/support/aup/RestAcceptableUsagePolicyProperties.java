package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

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
@JsonFilter("RestAcceptableUsagePolicyProperties")
public class RestAcceptableUsagePolicyProperties extends RestEndpointProperties {
    @Serial
    private static final long serialVersionUID = -8102345678378393382L;
}

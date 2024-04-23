package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link AuditRestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-audit-rest", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AuditRestProperties")
public class AuditRestProperties extends RestEndpointProperties {

    @Serial
    private static final long serialVersionUID = 3893437775090452831L;

    /**
     * Make storage requests asynchronously.
     */
    private boolean asynchronous = true;
}

package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.support.RestEndpointProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link AuditRestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
public class AuditRestProperties extends RestEndpointProperties {

    private static final long serialVersionUID = 3893437775090452831L;

    /**
     * Make storage requests asymchronously.
     */
    private boolean asynchronous = true;
}

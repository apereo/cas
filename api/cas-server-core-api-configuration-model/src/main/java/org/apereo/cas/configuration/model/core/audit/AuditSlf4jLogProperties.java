package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link AuditSlf4jLogProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-audit", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class AuditSlf4jLogProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 4227475246873515918L;

    /**
     * Indicates whether audit logs should be recorded as a single-line.
     * <p>
     * By default, audit logs are split into multiple lines where each action and activity
     * takes up a full line. This is a more compact version.
     */
    private boolean useSingleLine;

    /**
     * Character to separate audit fields if single-line audits are used.
     */
    private String singlelineSeparator = "|";
    
    /**
     * Control and define fields that can be accepted by the audit log.
     * Accepted values are:
     * <ul>
     *     <li>{@code who}</li>
     *     <li>{@code what}</li>
     *     <li>{@code action}</li>
     *     <li>{@code application}</li>
     *     <li>{@code when}</li>
     *     <li>{@code user_agent}</li>
     *     <li>{@code client_ip}</li>
     *     <li>{@code server_ip}</li>
     *     <li>{@code geo_location}</li>
     *     <li>{@code headers}</li>
     * </ul>
     */
    private String auditableFields = "who,what,when,action,client_ip,server_ip,geo_location";


    /**
     * Decide whether Slf4j audits should be enabled.
     */
    private boolean enabled = true;
}

package org.apereo.cas.configuration.model.support.ntlm;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link NtlmProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-spnego")
@Slf4j
@Getter
@Setter
public class NtlmProperties implements Serializable {

    private static final long serialVersionUID = 1479912148936123469L;

    /**
     * The domain controller to retrieve if load balanced.
     * Otherwise retrieve the domain controller as a possible NT or workgroup.
     */
    private String domainController;

    /**
     * If specified, gets all domain controllers in the specified {@link #domainController}
     * and then filters hosts that match the pattern.
     */
    private String includePattern;

    /**
     * Indicates how the domain controller should be retrieved, whether matched
     * and filtered by a pattern or retrieved as possible NT or workgroup.
     */
    private boolean loadBalance = true;

    /**
     * The name of the authentication handler.
     */
    private String name;
}

package org.apereo.cas.configuration.model.core.sso;

import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * Configuration properties class for SSO services settings.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)

public class SingleSignOnServicesProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -1654647966370731722L;

    /**
     * Flag that indicates whether to allow SSO session with a missing target service.
     * <p>
     * By default, CAS will present a generic success page if the initial authentication request does not identify
     * the target application. In some cases, the ability to login to CAS without logging
     * in to a particular service may be considered a misfeature because in practice, too few users and institutions
     * are prepared to understand, brand, and support what is at best a fringe use case of logging in to CAS for the
     * sake of establishing an SSO session without logging in to any CAS-reliant service.
     */
    private boolean allowMissingServiceParameter = true;

    /**
     * A regular expression pattern that represents an application
     * which must have established a session with CAS already
     * before access to other applications can be allowed by CAS.
     * This is the initial mandatory/required application with which
     * the user must start before going anywhere else.
     * Services that establish a session with CAS typically do so
     * by receiving a service ticket from CAS.
     */
    @RegularExpressionCapable
    private String requiredServicePattern;
}

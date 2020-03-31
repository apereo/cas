package org.apereo.cas.configuration.model.core.sso;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Configuration properties class for {@code create.sso}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class SingleSignOnProperties implements Serializable {

    private static final long serialVersionUID = -8777647966370741733L;

    /**
     * Flag that indicates whether to create SSO session on re-newed authentication event.
     */
    private boolean createSsoCookieOnRenewAuthn = true;

    /**
     * Flag that indicates whether to allow SSO session with a missing target service.
     */
    private boolean allowMissingServiceParameter = true;

    /**
     * Indicates whether CAS proxy authentication/tickets
     * are supported by this server implementation.
     */
    private boolean proxyAuthnEnabled = true;

    /**
     * Indicates whether this server implementation should globally
     * support CAS protocol authentication requests that are tagged with "renew=true".
     */
    private boolean renewAuthnEnabled = true;

    /**
     * A regular expression pattern that represents an application
     * which must have established a session with CAS already
     * before access to other applications can be allowed by CAS.
     * This is the initial mandatory/required application with which
     * the user must start before going anywhere else.
     * Services that establish a session with CAS typically do so
     * by receiving a service ticket from CAS.
     */
    private String requiredServicePattern;
}

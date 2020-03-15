package org.apereo.cas.configuration.model.support.spnego;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link SpnegoAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-spnego")
@Getter
@Setter
@Accessors(chain = true)
public class SpnegoAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = 4513529663377430783L;
    /**
     * Jcifs Netbios cache policy.
     */
    private long cachePolicy = 600;

    /**
     * The Jcifs domain controller.
     */
    private String jcifsDomainController;

    /**
     * The Jcifs domain.
     */
    private String jcifsDomain;

    /**
     * The Jcifs password.
     */
    private String jcifsPassword;

    /**
     * The Jcifs service password.
     */
    private String jcifsServicePassword;

    /**
     * The Jcifs service principal.
     */
    private String jcifsServicePrincipal = "HTTP/cas.example.com@EXAMPLE.COM";

    /**
     * Spnego JCIFS timeout.
     */
    private String timeout = "PT5M";

    /**
     * The Jcifs netbios wins.
     */
    private String jcifsNetbiosWins;

    /**
     * The Jcifs username.
     */
    private String jcifsUsername;
}


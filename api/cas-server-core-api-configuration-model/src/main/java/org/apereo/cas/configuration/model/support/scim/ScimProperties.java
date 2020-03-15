package org.apereo.cas.configuration.model.support.scim;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link ScimProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-scim")
@Getter
@Setter
@Accessors(chain = true)
public class ScimProperties implements Serializable {

    private static final long serialVersionUID = 7943229230342691009L;

    /**
     * Indicate what version of the scim protocol is and should be used.
     */
    private long version = 2;

    /**
     * The SCIM provisioning target URI.
     */
    @RequiredProperty
    private String target;

    /**
     * Authenticate into the SCIM server/service via a pre-generated OAuth token.
     */
    @RequiredProperty
    private String oauthToken;

    /**
     * Authenticate into the SCIM server with a pre-defined username.
     */
    @RequiredProperty
    private String username;

    /**
     * Authenticate into the SCIM server with a pre-defined password.
     */
    @RequiredProperty
    private String password;
}

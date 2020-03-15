package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link SamlIdPLogoutProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
public class SamlIdPLogoutProperties implements Serializable {

    private static final long serialVersionUID = -4608824149569614549L;

    /**
     * Whether SLO logout requests are required to be signed.
     */
    private boolean forceSignedLogoutRequests = true;

    /**
     * Whether SAML SLO is enabled and processed.
     */
    private boolean singleLogoutCallbacksDisabled;
}

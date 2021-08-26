package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("SamlIdPLogoutProperties")
public class SamlIdPLogoutProperties implements Serializable {

    private static final long serialVersionUID = -4608824149569614549L;

    /**
     * Whether SLO logout responses should be sent
     * using a forced binding. Accepted values are:
     * <ul>
     *     <li>{@code urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST}</li>
     *     <li>{@code urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect}</li>
     * </ul>
     * If no binding is defined, all available SLO endpoints
     * found in the metadata will be consulted for logout responses.
     */
    private String logoutResponseBinding;

    /**
     * Whether SLO logout responses are required to be signed.
     */
    private boolean signLogoutResponse;

    /**
     * Whether SLO logout responses should be sent to service providers.
     */
    private boolean sendLogoutResponse = true;

    /**
     * Whether SLO logout requests are required to be signed.
     */
    private boolean forceSignedLogoutRequests = true;

    /**
     * Whether SAML SLO is enabled and processed.
     */
    private boolean singleLogoutCallbacksDisabled;
}

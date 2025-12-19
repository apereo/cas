package org.apereo.cas.configuration.model.support.saml.idp.profile;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SamlIdPSSOSimpleSignProfileProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
public class SamlIdPSSOSimpleSignProfileProperties extends SamlIdPBaseProfileProperties {
    @Serial
    private static final long serialVersionUID = -8100516679034234656L;
}

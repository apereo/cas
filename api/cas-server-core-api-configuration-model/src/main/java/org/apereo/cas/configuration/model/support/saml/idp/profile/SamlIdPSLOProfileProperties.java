package org.apereo.cas.configuration.model.support.saml.idp.profile;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link SamlIdPSLOProfileProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SamlIdPSLOProfileProperties")
public class SamlIdPSLOProfileProperties extends SamlIdPBaseProfileProperties {
    @Serial
    private static final long serialVersionUID = -8100516679034234656L;
}
